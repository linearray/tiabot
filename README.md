TIABot
======

An IRC bot for Total Information Awareness, written in Scala. Broadcasts BitBucket and Trello notifications. Designed to be run on Heroku, but really can be run anywhere.

Set **SERVER**, **NICK**, **CHANNEL** envvars, push to Heroku and set webhook URLs with BitBucket/Trello to ***`/bitbucket`*** and ***`/trello`*** respectively.

Send a GET request to the root of your Heroku app every 15 minutes or so to prevent your dyno from being suspended due to inactivity. For example you could put this into crontab:

> */15 * * * * curl http://mymightytiabot.herokuapp.com > /dev/null 2>&1


[![Circle CI](https://circleci.com/gh/linearray/tiabot.svg?style=svg)](https://circleci.com/gh/linearray/tiabot)
