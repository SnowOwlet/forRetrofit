package cn.udday.forretrofit.bean

data class UserInfoBean(
    val `data`: Data,
    val status: Boolean
)

data class Data(
    val Avatar: String,
    val BCoins: Int,
    val Birthday: String,
    val Coins: Int,
    val Exp: Int,
    val Followers: Int,
    val Followings: Int,
    val Gender: String,
    val RegDate: String,
    val Saves: Any,
    val Statement: String,
    val TotalLikes: Int,
    val TotalViews: Int,
    val Uid: Int,
    val Username: String,
    val Videos: List<Video>
)

data class Video(
    val Author: Int,
    val Channel: String,
    val Coins: Int,
    val Cover: String,
    val Description: String,
    val Id: Int,
    val Length: String,
    val Likes: Int,
    val Saves: Int,
    val Shares: Int,
    val Time: String,
    val Title: String,
    val Video: String,
    val Views: Int
)