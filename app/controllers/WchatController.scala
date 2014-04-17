package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import anorm._
import play.api.Play.current
import play.api.db.DB
import org.apache.http.impl.client.HttpClients
import org.apache.http.client.methods.HttpGet
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import scala.collection.mutable
import model.WeChatUser

/**
 * Created by GoldRatio on 3/19/14.
 */
object WchatController  extends Controller {
  val base_url = "https://api.weixin.qq.com/sns/oauth2/"
  val appID = "wxcb99fefdeb53350c"
  val appSecret = "e07f15672bf910dbe2e9d14ab7ffbd2a"

  val objectMapper = {
    val obj = new ObjectMapper
    obj.registerModule(DefaultScalaModule)
  }

  val httpClient = HttpClients.createDefault()

  val token = "8wc2UWxYyKnyESVcqxTSVAdV8"

  def verify(signature: String, timestamp: String, nonce: String, echostr: String) = Action { implicit request =>
    val tmpStr = List(token, timestamp, nonce).sortWith(_ < _).mkString
    val md = java.security.MessageDigest.getInstance("SHA-1")
    val signatureCal = md.digest(tmpStr.getBytes("UTF-8")).map("%02x".format(_)).mkString
    if(signature == signatureCal)
      Ok(echostr)
    else
      BadRequest
  }

  def getUser(code: Option[String], state: String) = Action { implicit request =>
    code match {
      case Some(code) =>
        val url = (base_url + "access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code")
          .format(appID, appSecret, code)
        val result = getDataFromUrl(url)
        result.get("openid") match {
          case Some(openID) =>
            System.out.print("find optid" + openID)
            val tokenUrl = (base_url + "refresh_token?appid=%s&grant_type=refresh_token&refresh_token=%s")
              .format(appID, result.get("refresh_token").get)
            val tokenResult = getDataFromUrl(tokenUrl)
            tokenResult.get("openid") match {
              case Some(openID) =>
                val infoUrl = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s&lang=zh_CN"
                  .format(tokenResult.get("access_token").get, openID)
                val test = getDataFromUrl(infoUrl, classOf[WeChatUser])
                System.out.print("get user" + test)
                Ok(test)
              case _ =>
                System.out.print("not find user")
                BadRequest
            }
          case _ =>
            System.out.print("not find user")
            BadRequest
        }
    }
  }

  def getDataFromUrl[T](url: String, valueType: Class[T]): T = {
    val httpGet = new HttpGet(url)
    val response = httpClient.execute(httpGet)
    try {
      response.getStatusLine.getStatusCode match {
        case 200 =>
          val content = response.getEntity().getContent
          objectMapper.readValue(content, valueType)
        case _ =>
          null
      }
    }
    finally {
      response.close()
    }
  }

  def getDataFromUrl(url: String) : Map[String, String] = {
    val httpGet = new HttpGet(url)
    val response = httpClient.execute(httpGet)
    try {
      response.getStatusLine.getStatusCode match {
        case 200 =>
          val content = response.getEntity().getContent
          objectMapper.readValue(content, classOf[Map[String, String]])
        case _ =>
          Map()
      }
    }
    finally {
      response.close()
    }
  }
}

object Test {

  val objectMapper = {
    val obj = new ObjectMapper
    obj.registerModule(DefaultScalaModule)
  }

  def main(args: Array[String]) {
    val result = objectMapper.readValue("{\"access_token\":\"ACCESS_TOKEN\",\n   " +
      "\"expires_in\":7200,\n   \"refresh_token\":\"REFRESH_TOKEN\",\n  " +
      " \"openid\":\"OPENID\",\n   \"scope\":\"SCOPE\"\n}", classOf[Map[String, String]])
    System.out.println(result)
  }

}
