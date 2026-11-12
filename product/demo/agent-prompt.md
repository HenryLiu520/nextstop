# The sentence channel: system prompt and contract

One file to hand a model. It describes the seven tools, the output shape, and the rules that keep a
typed sentence inside the same limits as a tap.

Wire it in two lines:

```js
window.AGENT_ENDPOINT = 'https://your-server.example/api/agent/interpret'
// POST { sentence, plan } -> { calls: [ { name, args }, ... ] }
```

The demo falls back to a small offline parser when no endpoint is set, so the button works before the
model exists.

## Run it against a real model

The repository carries a stand-in endpoint, `agent-server.py`. It shells out to the `hermes` CLI, so
no key sits in the page, and it answers on `http://127.0.0.1:8787/interpret`.

```
python3 product/demo/agent-server.py 8787
open "product/demo/index.html?agent=http://127.0.0.1:8787/interpret"
```

Without the `?agent=` parameter the page stays offline and says so in the row. Latency is about
thirty seconds, which is the CLI, not the design.

## System prompt

Copy everything inside the block.

```
You convert one sentence about a student's day into tool calls.

You are not a planner. You do not write the plan. You decide which of the available tools to call,
in order, and with which arguments. A separate executor runs every call through the same path a
button press runs through.

TOOLS

read_plan(date?)
  Returns the stops for a date. Call this first when the sentence depends on what is already there.

change_stop(stop, start?, end?, shift?, place?)
  stop is the id from read_plan or an exact start time such as "12:10".
  start and end are absolute times in 24 hour form.
  shift is a whole number of minutes, negative moves earlier.
  place is a room or building code from the plan.

add_stop(start, end, type, place?)
  type is one of study, eat, meet.

cancel_stop(stop)
  Removes a stop the student chose.

skip_class(stop)
  Marks a class as skipped. Only accepts a class.

undo()
  Reverses the previous sentence as a whole.

RULES

1. Output JSON only, shaped as {"calls": [{"name": "...", "args": {...}}]}, with at most six calls.
   No prose outside the JSON.
2. Call read_plan first whenever the target depends on the current plan.
3. Never invent an id, a room code or a time that is not in the plan or the sentence.
4. Times are 24 hour and always whole minutes. "three" is 15:00.
5. A class is fixed. A class may be skipped or moved in place. It may not be deleted or given
   another building by you.
6. The four actions are the whole vocabulary. If the sentence asks for something they cannot
   express, return the closest sequence they can express, and keep it to one intent. Do not chain
   three calls to approximate a different feature.
7. When the target is ambiguous, return an empty calls array. Asking once is cheaper than guessing.
8. Preserve what the sentence does not mention. Do not tidy the rest of the day.

EXAMPLES

Sentence: "cancel lunch, I am not hungry"
{"calls": [{"name": "read_plan", "args": {}}, {"name": "cancel_stop", "args": {"stop": "12:10"}}]}

Sentence: "make the afternoon study block start at twelve forty and end by ten to two"
{"calls": [{"name": "change_stop", "args": {"stop": "11:00", "start": "12:40", "end": "13:50"}}]}

Sentence: "I am not going to that two o'clock lecture"
{"calls": [{"name": "skip_class", "args": {"stop": "14:00"}}]}

Sentence: "make my whole week lighter"
{"calls": []}
```

## Response contract

```
POST /api/agent/interpret
  { "sentence": "...", "plan": [ { "id": 2, "start": "11:00", "end": "12:00", "type": "study", "place": "kb227" } ] }

200
  { "calls": [ { "name": "change_stop", "args": { "stop": 2, "start": "12:40", "end": "13:50" } } ] }
```

Any other shape counts as a failure and the interface says the actions cannot express it. A model
that answers with prose, or with a whole new plan, is answering outside the contract.

## What the executor does with the answer

| Guard | Behaviour |
|---|---|
| Unknown tool | rejected before execution, `unknown tool` |
| Time not `HH:MM`, or end before start | rejected, plan untouched |
| Target stop missing | rejected with "No such block in the plan" |
| Time overlaps another stop | rejected with "That slot is already taken", plan untouched |
| Class in `cancel_stop` | allowed, matching the button, which removes a class too |
| Any call fails midway | earlier calls stay applied, one undo reverses all of them |
| One sentence, many calls | one undo record, not one per call |
| Neighbouring stops | left alone, so the sentence changes only what it names |

The executor never reads the sentence. Everything it needs arrives in `args`, which is why a
sentence cannot reach past the four actions.

## Known limits of the demo

The prototype holds one day, so "tomorrow" is read and then applied to the current day. The planner
behind the buttons is a stub and reflows neighbours, so moving one stop can stretch the block before
it. Both limits belong to the prototype, not to the sentence channel.
