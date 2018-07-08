# Socket Protocol
This document describes the socket protocol implemented through an exhaustive series of examples of interactions between server and client.

## Main Actions

#### Login

Client:
```
{
	"command":"login",
	"nickname":<user_nickname>,
	"password":<user_token>
}
```
Server:
```
{
	"message":"game-room",
	"players":<list_of_players>
}
```

---

#### Reconnection

Client:
```
{
	"command":"login",
	"nickname":<user_nickname>,
	"password":<user_token>
}
```
Server: 
```
{
	"message":"game-info",
	"toolcards": {<toolcard_name>:<boolean_used>}, {3} },
	"public-goals": [<public-card_name>, {3}],
	"private-goal":<private-card_name>
}

```
```
{
	"message":"reconnect-info",
	"windows":"{<player_name>:<window_obj>, +},
	"round-track": [<dice>, *],
	"favor-token":"{<player-name>:<num-token>, +}",
	"toolcard": <active-toolcard>
}
```
<i>active-toolcard</i> could be empty if there was no active toolcard.
```
{
	"message":"round",
	"player":<player_name>,
	"draft-pool": [<dice_obj>, +],
	"new-round":<boolean>
}
```

---

#### Schema Choice
Server:
```
{
	"message":"schema-choice",
	"schemas": [schema_obj, +]}
```
Client:
```
{
	"command":"schema",
	"id":<int>
}
```
Server:
```
{
	"message":"schema-chosen",
	"content": {<player_name>:<schema_obj, +}
}
```

---

#### Place Dice
Client:
```
{
	"command":"place-dice",
	"dice":"<dice_obj>,
	"row":<int>,
	"column":<int>
}
```
Server:
```
{
	"message":"update-window",
	"nickname":<player_name>,
	"row":<int>,
	"column":<int>,
	"dice":<dice_obj>
}
```

---

#### Use Tool Card
Client:
```
{
	"command":"toolcard",
	"name":<toolcard_name>
}
```
##### Ask Message
Server:
```
{
	"message":<action_message>,
	"prompt":<prompt_string>,
	"rollback":<boolean>
}
```
Client:
```
{
	"message":<action_message>,
	"choice":<coordinate_obj>
}
```

Action_messages:
* `toolcard-dice-window`
* `toolcard-dice-draftpool`
* `toolcard-dice-roundtrack`
* `toolcard-plus-minus`
* `toolcard-dice-value`
* `move-dice-number`


##### Optional
Client:
```
{
	"command":<ask_command>,
	"choice": "rollback"
}
```
Server:
```
{
	"message":"rollback-ok",
}
```

---

#### Pass
Client:
```
{
	"command":"pass"
}
```

---

#### Logout
Client:
```
{
	"command":"logout"
}
```


## Notifications

#### Others' Login
Server:
```
{
	"message":"new-player",
	"nickname": <player_name>
}
```

---

#### Others' Logout
Server:
```
{
	"message":"quit",
	"nickname":<player_name>
}
```

---

#### Others' Suspention
Server:
```
{
	"message":"suspended",
	"player":<player_name
}
```

---

#### Game Over
Server:
```
{
	"message":"game-over",
	"scores": [<score_obj>, +]
}
```

---

#### Tool Card Use
Server:
```
{
	"message":"toolcard-used",
	"player":<player_name>,
	"toolcard":<toolcard_name>,
	"window":<window_obj>,
	"draft-pool": [<dice_obj>, +],
	"round-track": [<dice_obj>, +]
}
```

---

#### Round
Server:
```
{
	"message":"round",
	"player":<player_name>,
	"draft-pool": [<dice_obj>, +],
	"new-round":<boolean>
	"round-track": [<dice_obj>, +]
}
```
<i>round-track</i> only if <i>new-round</i>.

#### Show Dice
Server:
```
{
	"message":"show-dice",
	"dice":<dice_obj>
}
```

#### Alert Dice Draft Pool
```
{
	"message":"alert-dice",
	"dice":<dice_obj>
}
```

## Objects

#### Window Obj
```
{
	"schema":<schema_obj>,
	"mosaic":[[<dice_obj>, +], +],
	"firstDicePlaced":<boolean>
}
```

---

#### Schema Obj
```
{
	"difficulty":<int_difficulty>,
	"constraint":[[<constraint_obj>, +], +],
	"name":<schema_name>
}
```

---

#### Constraint Obj
```
{
	"color":<color>,
	"number":<int>
}
```
<i>color</i> optional, <i>number</i> 0 if constraint is on color.

---

#### Dice Obj
```
{
	"color":<color>,
	"value"<int>
}
```

---

#### Coordinate Obj
```
{
	"row":<int>,
	"color":<int>
}
```

---

#### Score Obj
```
{
	"player":<player_name>,
	"totalScore":<int>,
	"privateScore":<int>,
	"publicScore":<int>,
	"favorToken":<int>,
	"emptyCells":<int>,
	"roundPosition":<int>
}
```
