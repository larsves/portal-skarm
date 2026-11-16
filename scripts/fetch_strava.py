"""Hämtar de senaste 14 dagarnas Strava-pass och skriver training.json.

Körs av GitHub Actions. Kräver miljövariablerna
STRAVA_CLIENT_ID, STRAVA_CLIENT_SECRET och STRAVA_REFRESH_TOKEN.
EASY_HR (valfri, standard 150): pass med snittpuls under detta räknas som lugna.
"""
import json
import os
import time
import urllib.parse
import urllib.request
from datetime import datetime, timezone


def post(url, data):
    req = urllib.request.Request(url, data=urllib.parse.urlencode(data).encode(), method="POST")
    with urllib.request.urlopen(req, timeout=30) as r:
        return json.load(r)


def get(url, token):
    req = urllib.request.Request(url, headers={"Authorization": f"Bearer {token}"})
    with urllib.request.urlopen(req, timeout=30) as r:
        return json.load(r)


def main():
    tok = post("https://www.strava.com/oauth/token", {
        "client_id": os.environ["STRAVA_CLIENT_ID"],
        "client_secret": os.environ["STRAVA_CLIENT_SECRET"],
        "grant_type": "refresh_token",
        "refresh_token": os.environ["STRAVA_REFRESH_TOKEN"],
    })
    after = int(time.time()) - 14 * 86400
    acts = get(f"https://www.strava.com/api/v3/athlete/activities?after={after}&per_page=100",
               tok["access_token"])

    out = []
    for a in acts:
        out.append({
            "date": a["start_date_local"][:10],
            "name": a.get("name") or a.get("sport_type", "Pass"),
            "type": a.get("sport_type"),
            "minutes": round(a.get("moving_time", 0) / 60, 1),
            "avg_hr": a.get("average_heartrate"),
        })
    out.sort(key=lambda x: x["date"])

    data = {
        "updated": datetime.now(timezone.utc).astimezone().strftime("%Y-%m-%dT%H:%M"),
        "easy_hr": int(os.environ.get("EASY_HR") or 150),
        "activities": out,
    }
    with open("training.json", "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=1)
    print(f"Skrev {len(out)} pass")


if __name__ == "__main__":
    main()
