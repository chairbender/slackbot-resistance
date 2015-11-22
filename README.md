# slackbot-resistance
A Slack bot for playing a game of The Resistance, a social deduction game for 5 - 10 players. Runs as a simple command line program
that connects to your Slack instance. Includes files for running it on Heroku.

## Heroku Installation
Requires:
* A [Slack](https://slack.com) account
* A free [Heroku](https://www.heroku.com/) account
* Permission to set up integrations in Slack

Please note that, if you are using the free Heroku account, the app will ocassionally go to sleep and the bot will
stop responding to you. Visit the URL for your Heroku app to wake it up. There's no way to keep it awake 24/7 without paying.
See the next section for info on running it yourself outside of Heroku.

1. Set up a Slack bot. In Slack, select "Configure Integrations".

    ![](http://i.imgur.com/knODtaX.png)
2. Select the "Configured Integrations Tab". Expand "Bots" and click "Add". Give it a name like "resistbot". Save that API Token for the next steps.
3. Click this button to set up a Heroku app to run the bot: [![Deploy](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy).
  * For the API_TOKEN variable, paste the API Token you copied from step 1 for the bot integration. For the BOT_USERNAME, use the name you gave to the bot in step 1.
4. With the Heroku app running, invite the bot to any channels you want it to listen on.

## Custom Installation
1. Ensure that you have installed [Gradle](http://gradle.org/gradle-download/) and added it to your PATH.
2. Clone this repository and run "gradle jar" in the root folder.
3. In Slack, configure a bot integration and save the API Key and Bot Name.
4. Run the jar with "java -jar build/libs/slackbot-resistance-1.0.jar (bot API key) (bot name) false (server port)"
5. You should be able to visit http://localhost:(server port) and see a small information page telling you that the bot is running.

## Usage
* 'resistbot' (assuming you called it 'resistbot') in any channel the bot is in and it will tell you what you can do
* 'resistbot start' to start a game in the current channel. You can run multiple games in different channels. The bot will
let you know what to do as you play.
* 'resistbot stop all games' to stop all current games in all channels
* 'resistbot stop this game' to stop the game in the current channel


## Contributing

Please open a new issue to provide feedback. I'd really appreciate feedback!
