# slackbot-resistance
A Slack bot for playing a game of The Resistance, a social deduction game for 5 - 10 players.

## Requirements
* A [Slack](https://slack.com) account
* A free [Heroku](https://www.heroku.com/) account
* Permission to set up integrations in Slack

## Installation

1. Set up a Slack bot. In Slack, select "Configure Integrations". Select the "Configured Integrations Tab".
Expand "Bots" and click "Add". Give it a name like "resistbot". Save that API Token for the next steps.
2. Click this button to set up a Heroku app to run the bot: [![Deploy](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy).
  * For the API_TOKEN variable, paste the API Token you copied from step 1 for the bot integration. For the BOT_USERNAME, use the name you gave to the bot in step 1.
3. Turn on the bot. When done deploying, click the "Manage App" button. Click the "Resources" tab. Click the 
pencil icon on the "bot" dyno. Click the switch to turn it on. Click "Confirm".
![](http://i.imgur.com/U1nDzt4.png)
4. With the Heroku app running, invite the bot to any channels you want it to listen on.

## Usage
* 'resistbot' (assuming you called it 'resistbot') in any channel the bot is in and it will tell you what you can do
* 'resistbot start' to start a game in the current channel. You can run multiple games in different channels. The bot will
let you know what to do as you play.
* 'resistbot stop all games' to stop all current games in all channels
* 'resistbot stop this game' to stop the game in the current channel


## Contributing

Please open a new issue to provide feedback. I'd really appreciate feedback!
