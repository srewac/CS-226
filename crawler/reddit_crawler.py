import os
import sys
import praw
import json
import datetime

CLIENT_ID = []
SECRET_KEY = []


if len(sys.argv) == 3:
    start_index = int(sys.argv[1])
    if sys.argv[2] == 'end':
        end_index = len(SUBREDDIT)
    else:
        end_index = int(sys.argv[2])
else:
    print("Usage: 'python3 [filename] [start index] [end index | 'end']'")
    exit()     
    
reddit = praw.Reddit(client_id=CLIENT_ID,
                     client_secret=SECRET_KEY,
                     redirect_uri='http://localhost:8080',
                     user_agent='test')
                     
lines = open("subreddits.txt").read().replace("'", '"').replace("\n", "")
SUBREDDITS = json.loads(lines)
SUBREDDITS = SUBREDDITS[start_index:end_index]        

# Fields from PRAW documentation (excluding class instance returns and CSS)
SUBMISSION_FIELDS = ('clicked', 'created_utc', 'distinguished',
                     'edited', 'id', 'is_video', 'locked', 
                     'num_comments', 'over_18', 'permalink', 
                     'score', 'selftext', 'stickied', 
                     'subreddit_id', 'title')
                     
COMMENT_FIELDS = ('body', 'created_utc', 'distinguished', 'edited',
                  'id', 'is_submitter', 'link_id', 'parent_id',
                  'permalink', 'score', 'stickied', 'subreddit_id')
                  
# not used
USER_FIELDS = ('comment_karma', 'created_utc', 'has_verified_email',
               'id', 'link_karma', 'name')

TOP_LIMIT = 700
MORE_COMMENT_LIMIT = 0

DELETED_AUTHOR = None
ERROR_LOG_FILENAME = "error_log.txt"
CRAWL_HIST_FILENAME = "crawl_history.txt"

DATA_PATH = "data"

for subreddit in SUBREDDITS:
    subreddit_path = os.path.join(DATA_PATH, subreddit)
    if not os.path.exists(subreddit_path):
        os.makedirs(subreddit_path)
    
    for submission in reddit.subreddit(subreddit).top(limit=TOP_LIMIT):
        submission_path = os.path.join(subreddit_path, submission.id)
        
        if not os.path.exists(submission_path):
            os.makedirs(submission_path)
            try:
                to_dict = vars(submission)
                sub_dict = {field:to_dict[field] for field in SUBMISSION_FIELDS}

                if not submission.is_self:
                    sub_dict['url'] = submission.url
                else:
                    sub_dict['url'] = None

                try:
                    sub_dict['author_name'] = submission.author.name
                    sub_dict['author_id'] = submission.author.id
                except:
                    sub_dict['author_name'] = DELETED_AUTHOR
                    sub_dict['author_id'] = DELETED_AUTHOR

                sub_dict['subreddit_name'] = subreddit
                sub_dict['upvote_ratio'] = submission.upvote_ratio
                
                # expensive when MORE_COMMENT_LIMIT > 0
                submission.comments.replace_more(limit=MORE_COMMENT_LIMIT)
                comments_list = submission.comments.list()

                comments_parsed_list = []
                comments_filename = "COMMENTS_" + sub_dict['id']
                comments_filepath = os.path.join(submission_path,
                                                 comments_filename)
                for comment in comments_list:
                    try:
                        to_dict = vars(comment)
                        comment_dict = {field:to_dict[field] for field in COMMENT_FIELDS}

                        try:
                            comment_dict['author_name'] = comment.author.name
                            comment_dict['author_id'] = comment.author.id
                        except:
                            comment_dict['author_name'] = DELETED_AUTHOR
                            comment_dict['author_id'] = DELETED_AUTHOR

                        comment_dict['subreddit_name'] = subreddit
                        comments_parsed_list.append(comment_dict)
                    except:
                        pass # a comment is skippable

                submission_filename = "SUBMISSION_" + sub_dict['id'] + ".json"
                submission_filepath = os.path.join(submission_path,
                                                   submission_filename)
                
                comments_filename = "COMMENTS_" + sub_dict['id'] + ".json"
                comments_filepath = os.path.join(submission_path,
                                                 comments_filename)

                with open(submission_filepath, 'w') as outfile:
                    json.dump(sub_dict, outfile)
                    
                with open(comments_filepath, 'w') as outfile:
                    json.dump(comments_parsed_list, outfile)
                    
            except Exception as e:
                # a whole submission has more information, so we'd like to know what went wrong
                with open(ERROR_LOG_FILENAME, "a") as error_log:
                    error_log.write("[" + str(datetime.datetime.now()) + "] Error at submission " + 
                                    str(submission.id) + " with error \"" + str(e) + "\"\n")
                    break
                    
    with open(CRAWL_HIST_FILENAME, "a") as crawl_hist:
        completed_sub = "Process " + str(os.getpid()) + " is done with subreddit " + subreddit
        print(completed_sub)
        crawl_hist.write(completed_sub + "\n")
       
