## Socket Protocol

s: \<welcome_message>CR

c: login\<TAB>\<nickname>CR

//new user login  
s: login\<TAB>\<new_user_nickname>\<TAB>\<auth_token>CR

//existing user login  
s: login\<TAB>\<existing_nickname>\<TAB>tokenCR  
c: token\<TAB>\<user_token>CR  
s: \[verified | failed]CR   

//after log  
s: lobbyplayers:\<TAB>\<player_list>CR  

//on new player connection  
s: newplayer:\<TAB>\<player>CR  

//start game  
s: game\<TAB>\<game_info_message>CR

//private goal extracted  
s: privateGoal\<TAB><privateGoalID>CR

//schema choice  
s: schemas\<TAB>\[\<schemaID>]<sup>4</sup>CR  
c: schema\<TAB>\<schemaID>CR  
s: \[ok|invalid]CR  

//public goal choice  
s: publicGoal\<TAB>\<publicGoalID>\<TAB>\<publicGoalID>\<TAB>\<publicGoalID>CR

//new round  
s: round\<TAB>\<round_number>\<TAB>\<nickname>CR  
c: extractCR  
s: dice\[\<TAB>\<dice_extracted>]<sup>+</sup>CR  

//exhaustive user turn cases

//place a dice and pass  
s: your-turnCR  
c: place\<TAB>\<dice>\<TAB>\<row>\<TAB>\<column>\<TAB>\<option>CR  
s: \[ok|invalid_dice|constrains_violated|bad_request]CR  
c: passCR  

//use tool card 1 and place dice  
s: your-turnCR  
c: active_tool_card\<TAB>\<toolCardID>CR  
s: \[ok|not_enough_token|not_valid_choice]CR  
c: place\<TAB>\<dice>\<TAB>\<row>\<TAB>\<column>\<TAB>\[+|-1]CR  
s: \[ok|invalid_dice|constrains_violated|bad_request]CR  
c: passCR

//use tool card 1, change your mind, use tool card 2 and pass  
s: your-turnCR  
c: active_tool_card\<TAB>\<toolCardID>CR  
s: \[ok|not_enough_token|not_valid_choice]CR  
c: deactive_tool_cardCR  
s: \[ok|no]CR  
c: active_tool_card\<TAB>\<toolCardID>CR  
s: \[ok|not_enough_token|not_valid_choice]CR  
c: move\<TAB>\<row>\<TAB>\<column>\<TAB>\<row>\<TAB>\<column>\<TAB>CR  
s: \[ok|constraints_violated|empty_cell|cell_already_occupied]CR  
c: passCR

//use tool card 4, then place a dice  
s: your-turnCR  
c: active_tool_card\<TAB>\<toolCardID>CR  
s: \[ok|not_enough_token|not_valid_choice]CR  
c: move\<TAB>\<row>\<TAB>\<column>\<TAB>\<row>\<TAB>\<column>\<TAB>CR  
c: move\<TAB>\<row>\<TAB>\<column>\<TAB>\<row>\<TAB>\<column>\<TAB>CR  
s: \[ok|constraints_violated|empty_cell|cell_already_occupied]CR  
c: place\<TAB>\<dice>\<TAB>\<row>\<TAB>\<column>CR  
s: \[ok|invalid_dice|constrains_violated|bad_request]CR  
c: passCR  

//use tool card 5 and place a dice  
s: your-turnCR  
c: active_tool_card\<TAB>\<toolCardID>CR  
s: \[ok|not_enough_token|not_valid_choice]CR  
c: swap_dice\<TAB><dice>\<TAB><dice_track>CR  
s: [ok|invalid_dice]CR  
c: place\<TAB>\<dice>\<TAB>\<row>\<TAB>\<column>CR  
s: \[ok|invalid_dice|constrains_violated|bad_request]CR  
c: passCR  
  
//use tool card 6 and place a dice  
s: your-turnCR  
c: active_tool_card\<TAB>\<toolCardID>CR  
s: \[ok|not_enough_token|not_valid_choice]CR  
c: reroll_dice\<TAB><dice>CR
s: new_dice\<TAB><dice>CR  
c: place\<TAB>\<dice>\<TAB>\<row>\<TAB>\<column>CR  
s: \[ok|invalid_dice|constrains_violated|bad_request]CR  
c: passCR  
  
//use tool card 7 and pass
s: your-turnCR  
c: active_tool_card\<TAB>\<toolCardID>CR  
s: \[ok|not_enough_token|not_valid_choice]CR  
c: reroll_allCR  
s: dice_list\[\<TAB>\<dice_rerolled>]<sup>+</sup>CR  
c: passCR  

//use tool card 8 and place two dices  
s: your-turnCR  
c: active_tool_card\<TAB>\<toolCardID>CR  
s: \[ok|not_enough_token|not_valid_choice]CR  
c: place\<TAB>\<dice>\<TAB>\<row>\<TAB>\<column>CR  
s: \[ok|invalid_dice|constrains_violated|bad_request]CR  
c: passCR  
s: your-turnCR  
c: place\<TAB>\<dice>\<TAB>\<row>\<TAB>\<column>CR  
s: \[ok|invalid_dice|constrains_violated|bad_request]CR  
c: passCR  

// use tool card 11 and place dice  
s: your-turnCR  
c: active_tool_card\<TAB>\<toolCardID>CR  
s: \[ok|not_enough_token|not_valid_choice]CR  
c: put_in_bag\<TAB><dice>CR  
s: dice_from_pool\<TAB><dice>CR  
c: set_value\<TAB><value>CR  
s: [ok|not_valid]CR  
c: place\<TAB>\<dice>\<TAB>\<row>\<TAB>\<column>CR  
s: \[ok|invalid_dice|constrains_violated|bad_request]CR  
c: passCR  
s: your-turnCR  
c: place\<TAB>\<dice>\<TAB>\<row>\<TAB>\<column>CR  
s: \[ok|invalid_dice|constrains_violated|bad_request]CR  
c: passCR  
  
//round done  
s: endRound\<TAB>\<dice_added_to_tracker>CR 

//game done  
s: endGame\<TAB>\[\<nickname>\<TAB>\<score>]<sup>+</sup>CR
