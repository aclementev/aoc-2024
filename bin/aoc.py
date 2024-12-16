#!/usr/bin/env -S uv run -q
# /// script
# requires-python = ">=3.12"
# dependencies = [
#     "httpx",
# ]
# ///

import argparse
import os
import pathlib
import subprocess

import httpx

# TODO(alvaro): Automatically figure out the day based on the context (last downloaded day in `data/`)
# TODO(alvaro): Result submission


class Day(int):
    pass


def parse_day(day: int) -> Day:
    if 1 <= day <= 25:
        return Day(day)
    raise ValueError(
        f"Invalid day for Advent of Code: {day} (type: {day.__class__.__name__})"
    )


class AoCClient:
    def __init__(self):
        session_cookie = os.environ.get("AOC_SESSION")
        if not session_cookie:
            raise RuntimeError(
                "No AoC session cookie found in environment. Fetch it from a logged in session in your browser, using the devtools to find the right cookie (i.e. in Chrome open the devtools, go to Application > Storage > Cookies > https://adventofcode.com > session)"
            )

        self.client = httpx.Client(cookies={"session": session_cookie})

    def get_input(self, day: Day) -> pathlib.Path:
        # Check if the input already exists
        input_path = data_path(day) / "input.txt"
        if input_path.exists():
            return input_path

        # Download the data
        response = self.client.get(f"https://adventofcode.com/2024/day/{day}/input")

        # Make sure that the path exists
        input_path.parent.mkdir(parents=True, exist_ok=True)

        with input_path.open("w") as f:
            f.write(response.text)

        return input_path


def repo_head() -> pathlib.Path:
    path = subprocess.check_output(["git", "rev-parse", "--show-toplevel"], text=True)
    return pathlib.Path(path.strip())


def data_path(day: Day) -> pathlib.Path:
    return repo_head() / "data" / f"day{day:02}"


def download(day: int) -> None:
    day = parse_day(day)
    client = AoCClient()
    input_path = client.get_input(day)
    print(f"Downloaded input to: {input_path.resolve()}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    subparser = parser.add_subparsers(dest="subcommand")

    down_parser = subparser.add_parser("download", aliases=["d"])
    # TODO(alvaro): Automatically figure out the last day that has been downloaded by default
    # and download the next one
    down_parser.add_argument(
        "day", help="Day of the Advent of Code to download", type=int, default=None
    )

    args = parser.parse_args()
    match args.subcommand:
        case "download":
            download(args.day)
