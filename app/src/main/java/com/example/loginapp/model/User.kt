package com.example.loginapp.model

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable

@DynamoDBTable(tableName = "LoginAppUser")
class User {
    @get:DynamoDBHashKey(attributeName = "UserId")
    var userId: String? = null

    @get:DynamoDBRangeKey(attributeName = "Password")
    var password: String? = null

    constructor(
        userId: String?,
        password: String?,
    ) {
        this.userId = userId
        this.password = password
    }

    constructor()
}