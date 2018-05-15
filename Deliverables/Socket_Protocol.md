# Socket Protocol

s: <welcome_message>CR
c: login<TAB><nickname>CR

//new user login
s: login<TAB><new_user_nickname_with_his_token>CR

//existing user login
s: login<TAB><existing_nickname><TAB>tokenCR
c: token<TAB><user_token> CR
s: <user_token_verification_result>CR

//start game
s: game<TAB><game_info_message>CR

//private goal extracted
s: privateGoal<TAB><privateGoalID>CR

//window choice
s: window<TAB>[<windowID>]+CR
c: window<TAB><windowID>CR

//public goal choice
s: publicGoal<TAB><privateGoalID>>TAB><privateGoalID><TAB><privateGoalID>CR

//new round
s: round<TAB><round_number><TAB><nickname>CR
c: extractCR
s: dice [<dice_extracted>]+CR

//user passes
c: passCR

//user chooses dice (option: flag to indicate dice chose from draft pool or from schema)
c: place<TAB><dice><TAB><row><TAB><column><TAB><option>CR
s: <result_message>CR 

//user chooses tool card
c: toolCard<TAB><toolCardID>CR
s: <result_message>CR 

//round done
s: endRound<TAB><dice_added_to_tracker>CR 

//game done
s: endGame<TAB>[<nickname><TAB><score>]+CR

//exchange dice on schema with one on tracker
c: place<TAB><dice><TAB><row><TAB><column><TAB><tracker_index>CR
s: <result_message>CR 
