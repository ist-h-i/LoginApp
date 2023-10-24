package com.example.loginapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMappingException
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.example.loginapp.databinding.ActivityMainBinding
import com.example.loginapp.model.User

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 認証情報の取得
        val credentialsProvider = CognitoCachingCredentialsProvider(
            this, "ap-northeast-1:xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
            Regions.AP_NORTHEAST_1 // Region
        )

        // DynamoDBクライアント
        val ddbClient: AmazonDynamoDB = AmazonDynamoDBClient(credentialsProvider)
        // デフォルトではUS-EASTリージョンでクライアント作成されてしまうため明示的にAP_NORTHEASTに設定
        ddbClient.setRegion(Region.getRegion(Regions.AP_NORTHEAST_1))
        val ddbMapper = DynamoDBMapper.builder().dynamoDBClient(ddbClient).build()

        // レイアウト紐付け
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ログインボタン押下時の設定
        binding.loginButton.setOnClickListener {
            val userId: String = binding.userId.text.toString()
            val password: String = binding.password.text.toString()

            // 未入力欄がある場合
            if (userId.isBlank() || password.isBlank()) {
                showLongToast(R.string.inputUserInfo)
                return@setOnClickListener
            }

            // DynamoDBへのQuery属性
            val eav = HashMap<String, AttributeValue>()
            eav[":v1"] = AttributeValue().withS(userId)
            eav[":v2"] = AttributeValue().withS(password)

            val countExpression =
                DynamoDBScanExpression().withFilterExpression("UserId = :v1 AND Password = :v2")
                    .withExpressionAttributeValues(eav)

            // 外部通信のためスレッド生成
            val queryDynamoDb = Thread {
                try {
                    // Query結果
                    val itemCount: Int = ddbMapper.count(User::class.java, countExpression)
                    if (itemCount != 0) {
                        // User情報が登録済みの場合
                        showLongToast(R.string.loginSuccess)
                        // ログイン後後続処理はこの下に記載
                    } else {
                        // User情報がない場合
                        showLongToast(R.string.loginFailed)
                    }
                } catch (error: DynamoDBMappingException) {
                    showLongToast(R.string.unknownError)
                }
            }
            queryDynamoDb.start()
            try {
                queryDynamoDb.join()
            } catch (error: InterruptedException) {
                Toast.makeText(this, R.string.unknownError, Toast.LENGTH_LONG).show()
            }

        }
    }

    private fun showLongToast(messageInt: Int) {
        // 別スレッドからUIを操作
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            Toast.makeText(this, messageInt, Toast.LENGTH_LONG).show()
        }
    }
}