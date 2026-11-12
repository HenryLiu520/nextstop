#!/usr/bin/env python3
"""A stand-in model endpoint for the demo.

POST /interpret {sentence, plan} -> {calls: [...]}

It shells out to the hermes CLI so no key ever reaches the page script.
"""
import json
import pathlib
import subprocess
import sys
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

HERE = pathlib.Path(__file__).parent

SYSTEM = """You convert one sentence about a student's day into tool calls.
Output JSON only: {"calls":[{"name":"...","args":{...}}]}, at most six calls, no prose.

Tools:
read_plan()                        list the stops
change_stop(stop,start?,end?,shift?,place?)   stop is an id or an exact start time
add_stop(start,end,type,place?)     type is study, eat, meet
cancel_stop(stop)
skip_class(stop)                   only accepts a class
undo()

Rules: prefer the smallest edit that satisfies the sentence. A class may be skipped
only when the sentence says the student will not attend. Times are 24 hour HH:MM. A class may be skipped or moved, never deleted or
rebuilt. Preserve everything the sentence does not mention. Return {"calls":[]} only
when nothing in the plan matches the sentence. If the target is ambiguous, pick the
stop the sentence most plausibly names and say so through the args you choose. If the
four actions cannot express the request, return the closest single intent they can.

Context: the plan below is the only day available. Read "today", "tomorrow" and
"this afternoon" as this plan. A place named in words ("the lab", "the library") may
be passed to change_stop as place, even when the code is unknown."""


def build_prompt(sentence, plan):
    return (
        SYSTEM
        + "\n\nCurrent plan (JSON):\n"
        + json.dumps(plan, ensure_ascii=False)
        + "\n\nSentence: "
        + sentence
        + "\n\nAnswer with the JSON object only."
    )


def extract_json(text):
    start = text.find("{")
    while start != -1:
        depth = 0
        for i in range(start, len(text)):
            if text[i] == "{":
                depth += 1
            elif text[i] == "}":
                depth -= 1
                if depth == 0:
                    try:
                        return json.loads(text[start:i + 1])
                    except json.JSONDecodeError:
                        break
        start = text.find("{", start + 1)
    return None


def ask_model(sentence, plan):
    prompt = build_prompt(sentence, plan)
    proc = subprocess.run(
        ["hermes", "-z", prompt, "--provider", "zai", "-m", "glm-4.6"],
        capture_output=True, text=True, timeout=180,
    )
    out = (proc.stdout or "") + "\n" + (proc.stderr or "")
    data = extract_json(out)
    if not data or "calls" not in data:
        return [], out.strip()[-400:]
    return data["calls"][:6], ""


class Handler(BaseHTTPRequestHandler):
    def _cors(self):
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Headers", "Content-Type")
        self.send_header("Access-Control-Allow-Methods", "POST, OPTIONS")

    def do_OPTIONS(self):
        self.send_response(204)
        self._cors()
        self.end_headers()

    def do_GET(self):
        # 同一个端口把页面也发出去，页面和接口同源，就不存在跨域这回事
        if self.path in ("/", "/index.html"):
            page = (HERE / "index.html").read_bytes()
            self.send_response(200)
            self.send_header("Content-Type", "text/html; charset=utf-8")
            self.send_header("Cache-Control", "no-store")
            self.end_headers()
            self.wfile.write(page)
            return
        body = json.dumps({"ok": True, "hint": "POST /interpret"}).encode()
        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self._cors()
        self.end_headers()
        self.wfile.write(body)

    def do_POST(self):
        n = int(self.headers.get("Content-Length", "0"))
        payload = json.loads(self.rfile.read(n) or b"{}")
        sentence = payload.get("sentence", "")
        plan = payload.get("plan", [])
        try:
            calls, note = ask_model(sentence, plan)
        except subprocess.TimeoutExpired:
            calls, note = [], "model timeout"
        print(f"[{sentence[:60]}] -> {len(calls)} calls {note}", flush=True)
        body = json.dumps({"calls": calls}).encode()
        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self._cors()
        self.end_headers()
        self.wfile.write(body)

    def log_message(self, *a):
        pass


if __name__ == "__main__":
    port = int(sys.argv[1]) if len(sys.argv) > 1 else 8787
    print(f"open http://127.0.0.1:{port}/  (page and endpoint share an origin)", flush=True)
    ThreadingHTTPServer(("127.0.0.1", port), Handler).serve_forever()
