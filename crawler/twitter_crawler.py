from tweepy import Stream
from tweepy import OAuthHandler
from tweepy.streaming import StreamListener
import tweepy
import MySQLdb
import time
import json
import sys
import twitter

# https://python-twitter.readthedocs.io/en/latest/twitter.html

ckey = [Token]
csecret = [Token]
atoken = [Token]
asecret = [Token]
auth = OAuthHandler(ckey, csecret)
auth.set_access_token(atoken, asecret)
api = tweepy.API(auth)

# search = api.search(q="#frozen2", lang='en', include_entities=True, count=2)  
# print(search)
out_file = "./data/twitter_data.json"

max_tweets = 20000

searched_tweets = []
last_id = -1
itr = 0
while len(searched_tweets) < max_tweets:
    count = max_tweets - len(searched_tweets)
    try:
        new_tweets = api.search(q="#frozen2 -filter:retweets", count=count, tweet_mode="extended",
                                max_id=str(last_id - 1), lang='en')
        if not new_tweets:
            break
        start = searched_tweets.__len__()
        searched_tweets.extend(new_tweets)
        last_id = new_tweets[-1].id
        end = searched_tweets.__len__()
        f = open("data", 'a')
        for i in range(start, end):
            body = searched_tweets[i]
            print("Twitter ;", end='', file=f)
            print(body.full_text.replace('\n', ' ').split(' '), file=f)
        itr += 1
        print(end)
        print(itr)
        time.sleep(60)
    except tweepy.TweepError as e:
        # depending on TweepError.code, one may want to retry or wait
        # to keep things simple, we will give up on an error
        print("Error")
        break

print(searched_tweets.__len__())


