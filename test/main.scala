package tiabot

import org.specs2.mutable._

import scalaz._, Scalaz._
import argonaut._, Argonaut._


class TIABotSpec extends Specification {
  sequential

  "TIABot" should {

    "decode JSON" in {
      val json = """{"repository": {"website": "", "fork": false, "name": "testrepodeleteme", "scm": "git", "owner": "linearray", "absolute_url": "/linearray/testrepodeleteme/", "slug": "testrepodeleteme", "is_private": true}, "truncated": false, "commits": [{"node": "1d2d32b7141e", "files": [{"type": "modified", "file": "test.c"}], "raw_author": "Max Amanshauser <max@lambdalifting.org>", "utctimestamp": "2015-02-27 23:10:59+00:00", "author": "Max Amanshauser", "timestamp": "2015-02-28 00:10:59", "raw_node": "1d2d32b7141e4f523281d7ca02d27fe7913efb0a", "parents": ["6cebc351af57"], "branch": "master", "message": "aaaaaaaasdf\n", "revision": null, "size": -1}], "canon_url": "https://bitbucket.org", "user": "linearray"}"""

      val bb = json.decode[BitBucketPost]

      bb match {
        case \/-(res) => println(Format.formatMessage(res)); success
        case -\/(err) => anError
      }
    }

    "decode JSON for multiple commits" in {
      val json = """{"repository": {"website": "", "fork": false, "name": "testrepodeleteme", "scm": "git", "owner": "linearray", "absolute_url": "/linearray/testrepodeleteme/", "slug": "testrepodeleteme", "is_private": true}, "truncated": false, "commits": [{"node": "45441fc5720e", "files": [{"type": "modified", "file": "test.c"}], "branches": [], "raw_author": "Max Amanshauser <max@lambdalifting.org>", "utctimestamp": "2015-02-28 00:51:24+00:00", "author": "Max Amanshauser", "timestamp": "2015-02-28 01:51:24", "raw_node": "45441fc5720e6faaf96b4ee3719d0fa563fffc65", "parents": ["9f7c94ab1e85"], "branch": null, "message": "commit 1\n", "revision": null, "size": -1}, {"node": "afa08e9e2908", "files": [{"type": "modified", "file": "test.c"}], "raw_author": "Max Amanshauser <max@lambdalifting.org>", "utctimestamp": "2015-02-28 00:51:35+00:00", "author": "Max Amanshauser", "timestamp": "2015-02-28 01:51:35", "raw_node": "afa08e9e29083e7d605d7b18f9a6bbade85cf68a", "parents": ["45441fc5720e"], "branch": "master", "message": "commit 2\n", "revision": null, "size": -1}], "canon_url": "https://bitbucket.org", "user": "linearray"}"""

      val bb = json.decode[BitBucketPost]


      bb match {
        case \/-(res) => println(Format.formatMessage(res)); success
        case -\/(err) => anError
      }
    }
  }
}
