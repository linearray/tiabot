package tiabot

import java.io.{InputStreamReader, BufferedReader, BufferedWriter, OutputStreamWriter}
import java.net.{SocketException, Socket}
import scala.util.Random
import scala.util.Properties

import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.HttpService
import org.http4s.dsl._

import argonaut._, Argonaut._

import scalaz.{-\/, \/-}
import scalaz.concurrent.Task

object main {
  def main(args: Array[String]): Unit = TIABot.run("0.0.0.0",Properties.envOrElse("PORT","8080").toInt)
}


object IRCClient {
  val server   = scala.util.Properties.envOrElse("SERVER","irc.oftc.net")
  val baseNick = scala.util.Properties.envOrElse("NICK","TIABot")
  var nick     = baseNick
  val login    = baseNick
  val realName = scala.util.Properties.envOrElse("REAL_NAME","Total Information Awareness Bot")
  val channel  = scala.util.Properties.envOrElse("CHANNEL","#preciousbodilyfluids")
  val rand     = new Random(System.currentTimeMillis)

  var socket  = new Socket(server, 6667)
  var writer  = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream))
  var reader  = new BufferedReader(new InputStreamReader(socket.getInputStream))

  def restart() : Unit = {
    Thread.sleep(20000)
    socket  = new Socket(server, 6667)
    writer  = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream))
    reader  = new BufferedReader(new InputStreamReader(socket.getInputStream))
  }

  def shuffleNick() : Unit = {
    nick = baseNick + rand.nextInt.toString.substring(0,2).toInt
  }


  def run() : Unit = {
    writer.write("NICK " + nick + "\r\n")
    writer.write(s"USER $login 8 * : $realName\r\n")
    writer.flush()

    def loop() : Unit = {
      try {
        reader.readLine() match {
          case null =>
            println("readLine() is null")
            System.exit(0)

          case e if e.indexOf("433") >= 0 =>
            println("Nickname is already in use.")
            shuffleNick()
            restart()
            loop()

          case e if e.startsWith("PING ") =>
            writer.write("PONG " + e.substring(5) + "\r\n")
            writer.flush()
            loop()

          case e if e.indexOf("004") >= 0 =>
            println("successfully connected")
            loop()

          case e =>
            println(e)
            loop()
        }
      } catch {
        case s: SocketException => {
          Thread.sleep(20000)
          restart()
        }
      }
    }

    writer.write("JOIN " + channel + "\r\n")
    writer.flush()

    loop()
  }

  def post(str: String) = {
    println("posting message")
    writer.write(s"PRIVMSG ${channel} :" + str.trim() + "\r\n")
    writer.flush()
  }
}


object TIABot {
  def run(host: String, port: Int): Unit = {
    Task(IRCClient.run()).runAsync(_ => Unit)

    println("starting server")

    BlazeBuilder
      .bindHttp(port, host)
      .mountService(Services.bitbucket, "")
      .mountService(Services.trello,"")
      .run
  }
}


object Services {

  val bitbucket = HttpService {
    case req@POST -> Root / "bitbucket" =>
      req.decode[String] { str =>

        val res = java.net.URLDecoder.decode(str.substring(8), "UTF-8")
        val bbpost = res.decode[BitBucketPost]

        Task {
          bbpost match {
            case \/-(x) => Format.formatMessage(x).split('\n').foreach(IRCClient.post(_))
            case -\/(err) => println(err); IRCClient.post("no/wrong data received")
          }
        }.runAsync(_ => Unit)
        println(bbpost)

        Ok()
      }

    case req@GET -> Root=>
      println("keepalive")
      Ok()
  }


  val trello = HttpService {
    case req@GET  -> Root / "trello" =>
      println("got request")
      Ok()
    case req@HEAD -> Root / "trello" =>
      Ok()
    case req@POST -> Root / "trello" =>
      req.decode[String] { str =>
        val tr = str.decode[TrelloObject]

        tr match {
          case -\/(err) => println(err); BadRequest()
          case \/-(res) => {
            val trstring = Format.formatMessage(res)
            println(trstring)
            IRCClient.post(trstring)
            Ok()
          }
        }
      }
  }
}

object Format {
  def formatMessage(bb: BitBucketPost) = {
    s"${bb.user} pushed ${bb.commits.length} commit(s) to ${bb.repository.name}\n" + bb.commits.foldLeft(new String()) ((acc,c) => acc + s"${c.author}: ${c.message.takeWhile(_ != '\n')} - ${bb.canonUrl}${bb.repository.absoluteUrl}commits/${c.rawNode}\n")
  }

  def formatMessage(tr: TrelloObject) = {
    val res = s"${tr.board} edited by ${tr.user}: ${tr.action} event "

    res + ((tr.list, tr.card) match {
      case (Some(list),Some(card)) => s"on $list / $card"
      case (Some(list),None)       => s"on $list"
      case (None,Some(card))       => s"on $card"
      case (None,None)             => ""
    })
  }
}



//   _
//  (_)___  ___  _ __
//  | / __|/ _ \| '_ \
//  | \__ \ (_) | | | |
// _/ |___/\___/|_| |_|
//|__/

object BitBucketPost {
  implicit def BitBucketPostCodecJson : CodecJson[BitBucketPost] =
    casecodec5(BitBucketPost.apply, BitBucketPost.unapply)("repository","truncated","commits","canon_url","user")
}

case class BitBucketPost(
  repository  : Repository,
  truncated   : Boolean,
  commits     : List[Commit],
  canonUrl    : String,
  user        : String
)

object Repository {
  implicit def RepositoryCodecJson : CodecJson[Repository] =
    casecodec8(Repository.apply, Repository.unapply)("website","fork","name","scm","owner","absolute_url","slug","is_private")
}
case class Repository(
  website     : String,
  fork        : Boolean,
  name        : String,
  scm         : String,
  owner       : String,
  absoluteUrl : String,
  slug        : String,
  is_private  : Boolean
)

object Commit {
  implicit def CommitCodecJson : CodecJson[Commit] =
    casecodec12(Commit.apply, Commit.unapply)("node","files","raw_author","utctimestamp","author","timestamp","raw_node","parents","branch","message","revision","size")
}
case class Commit(
  node         : String,
  files        : List[File],
  rawAuthor    : String,
  utctimestamp : String,
  author       : String,
  timestamp    : String,
  rawNode      : String,
  parents      : List[String],
  branch       : Option[String],
  message      : String,
  revision     : Option[String],
  size         : Int
)

object File {
  implicit def FileCodecJson : CodecJson[File] =
    casecodec2(File.apply, File.unapply)("type","file")
}

case class File(`type`: String, file: String)


object TrelloObject {
  implicit def TrelloObjectDecodeJson: DecodeJson[TrelloObject] =
    DecodeJson(curse => for {
      board  <- (curse --\ "action" --\ "data" --\ "board" --\ "name").as[String]
      list   <- (curse --\ "action" --\ "data" --\ "list" --\ "name").as[Option[String]]
      card   <- (curse --\ "action" --\ "data" --\ "card" --\ "name").as[Option[String]]
      user   <- (curse --\ "action" --\ "memberCreator" --\ "username").as[String]
      action <- (curse --\ "action" --\ "type").as[String]
    } yield TrelloObject(board, list, card, user, action))
}

case class TrelloObject(board: String, list: Option[String], card: Option[String], user: String, action: String)
