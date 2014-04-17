package model

/**
 * Created by GoldRatio on 4/17/14.
 */
class User {

}

case class WeChatUser( openid: String, nickname: String, sex: String, province: String, city: String
                        , country: String, headimgurl: String,  privilege: List[String])
