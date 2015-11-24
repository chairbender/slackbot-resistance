# slackbot-resistance
A Slack bot for playing a game of The Resistance, a social deduction game for 5 - 10 players. Runs as a simple command line program
that connects to your Slack instance. Includes files for running it on Heroku.

## Running It
Requires Java 7 JRE (or higher) on the machine you want to run it on

1. Set up a Slack bot. In Slack, select "Configure Integrations".

    ![](http://i.imgur.com/knODtaX.png)
2. Select the "Configured Integrations Tab". Expand "Bots" and click "Add". Give it a name like "resistbot". Save that API Token for the next steps.
3. Download the [latest release JAR](https://github.com/chairbender/slackbot-resistance/releases/download/1.01/slackbot-resistance-1.01-standalone.jar) from the releases page.
4. From the command line, run the JAR like so: 
    java -jar (JAR name) (bot API key) (bot name)
    * For (JAR name), use the path to the JAR you downloaded in step 3.
    * Use the API Key you obtained from step 2 for (bot API key)
    * For (bot name), use the name you gave to the bot when setting up the integration.
5. Invite the bot to a channel and you should be able to use it. Say its name for more info.    

## Heroku Installation
Requires:
* A [Slack](https://slack.com) account
* A free [Heroku](https://www.heroku.com/) account
* Permission to set up integrations in Slack

Please note that, if you are using the free Heroku account, the app will ocassionally go to sleep and the bot will
stop responding to you. Visit the URL for your Heroku app to wake it up. YOU WILL LOSE YOUR CURRENT GAME! There's no way to keep it awake 24/7 without paying.
It may be easier to just run it yourself as described in the "Running It" section.

1. Set up a Slack bot. In Slack, select "Configure Integrations".

    ![](http://i.imgur.com/knODtaX.png)
2. Select the "Configured Integrations Tab". Expand "Bots" and click "Add". Give it a name like "resistbot". Save that API Token for the next steps.
3. Click this button to set up a Heroku app to run the bot: [![Deploy](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy).
  * For the API_TOKEN variable, paste the API Token you copied from step 1 for the bot integration. For the BOT_USERNAME, use the name you gave to the bot in step 1.
4. With the Heroku app running, invite the bot to any channels you want it to listen on.
5. Remember, if using the free Heroku, visit the URL for your Heroku app to wake the bot up should it go to sleep.


## Usage
* 'resistbot' (assuming you called it 'resistbot') in any channel the bot is in and it will tell you what you can do
* 'resistbot start' to start a game in the current channel. You can run multiple games in different channels. The bot will
let you know what to do as you play.
* 'resistbot stop all games' to stop all current games in all channels
* 'resistbot stop this game' to stop the game in the current channel


## Contributing

Please open a new issue to provide feedback. I'd really appreciate feedback!
