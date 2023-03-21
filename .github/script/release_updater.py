import json
import os
import requests
import aiohttp
from datetime import datetime

RELEASE_NOTES_URL = "https://api.github.com/repos/{}/{}/releases/tags/{}"
RELEASE_PROPERTIES_FILENAME = "../../gradle/release.properties"
VERSION_JSON_FILENAME = "../../app/.meta/version.json"
APP_ID = "com.mxt.anitrend"
MIN_SDK = 17

def get_latest_release_tag():
    response = requests.get("https://api.github.com/repos/{}/{}/releases/latest".format(os.getenv("GITHUB_REPOSITORY_OWNER"), os.getenv("GITHUB_REPOSITORY_NAME")))
    response.raise_for_status()
    return response.json()["tag_name"]

def get_release_notes(tag):
    response = requests.get(RELEASE_NOTES_URL.format(os.getenv("GITHUB_REPOSITORY_OWNER"), os.getenv("GITHUB_REPOSITORY_NAME"), tag), headers={"Accept": "application/vnd.github.v3+json"})
    response.raise_for_status()
    return response.json()["body"]

def get_existing_version_json():
    try:
        with open(VERSION_JSON_FILENAME) as f:
            return json.load(f)
    except FileNotFoundError:
        return None

def get_existing_release_properties():
    try:
        with open(RELEASE_PROPERTIES_FILENAME) as f:
            properties = {}
            for line in f:
                key, value = line.strip().split("=", 1)
                properties[key] = value
            return properties
    except FileNotFoundError:
        return None

def get_current_timestamp():
    return datetime.utcnow().isoformat() + "Z"

def get_code_from_tag(tag):
    major, minor, patch = [int(x) for x in tag[1:].split(".")]
    return major * 1000000000 + minor * 10000 + patch

def get_version_from_tag(tag):
    return tag[1:]

def get_latest_commit_sha():
    response = requests.get("https://api.github.com/repos/{}/{}/commits/{}".format(os.getenv("GITHUB_REPOSITORY_OWNER"), os.getenv("GITHUB_REPOSITORY_NAME"), os.getenv("GITHUB_SHA")))
    response.raise_for_status()
    return response.json()["sha"]

def get_branch_name():
    return os.getenv("GITHUB_REF", "").split("/")[-1]

def get_commit_messages():
    response = requests.get("https://api.github.com/repos/{}/{}/commits/{}/comments".format(os.getenv("GITHUB_REPOSITORY_OWNER"), os.getenv("GITHUB_REPOSITORY_NAME"), os.getenv("GITHUB_SHA")))
    response.raise_for_status()
    return [comment["body"] for comment in response.json()]

def get_commit_authors():
    response = requests.get("https://api.github.com/repos/{}/{}/commits/{}".format(os.getenv("GITHUB_REPOSITORY_OWNER"), os.getenv("GITHUB_REPOSITORY_NAME"), os.getenv("GITHUB_SHA")))
    response.raise_for_status()
    return [response.json()["author"]["login"]]

def get_modified_files():
    response = requests.get("https://api.github.com/repos/{}/{}/commits/{}/files".format(os.getenv("GITHUB_REPOSITORY_OWNER"), os.getenv("GITHUB_REPOSITORY_NAME"), os.getenv("GITHUB_SHA")))
    response.raise_for_status()
    return [file["filename"] for file in response.json() if file["status"] == "modified"]

def get_added_files():
    response = requests.get("https://api.github.com/repos/{}/{}/commits/{}/files".format(os.getenv("GITHUB_REPOSITORY_OWNER"), os.getenv("GITHUB_REPOSITORY_NAME"), os.getenv("GITHUB_SHA")))
    response.raise_for_status()
    return [file["filename"] for file in response.json() if file["status"] == "added"]

def get_removed_files(previous_commit, current_commit):
    """
    Returns a list of the files that have been removed between the previous_commit and the current_commit
    """
    command = f"git diff --name-only --diff-filter=D {previous_commit}...{current_commit}"
    result = os.popen(command).read().strip().split("\n")
    return result


def get_changed_files(previous_commit, current_commit):
    """
    Returns a list of the files that have been modified or added between the previous_commit and the current_commit
    """
    command = f"git diff --name-only {previous_commit}...{current_commit}"
    result = os.popen(command).read().strip().split("\n")
    return result


def write_to_file(filepath: str, data: dict):
    """
    Writes data to filepath as json.
    """
    with open(filepath, "w") as f:
        json.dump(data, f)

async def generate_release_files():
    # Get the latest tag
    tag = os.popen("git describe --abbrev=0 --tags").read().strip()

    # Parse the tag to get the version information
    version_info = tag.split(".")
    major_version, minor_version, patch_version = map(int, version_info)

    # Calculate the code based on the version
    code = major_version * 1000000000 + minor_version * 10000 + patch_version

    # Generate release.properties
    release_properties = f"version={tag}\ncode={code}"
    with open("release.properties", "w") as f:
        f.write(release_properties)

    # Generate version.json
    previous_tag = os.popen(f"git describe --abbrev=0 --tags {tag}^").read().strip()

    removed_files = get_removed_files(previous_tag, tag)
    changed_files = get_changed_files(previous_tag, tag)

    migration = False
    if any(file.endswith(".pb") for file in removed_files + changed_files):
        migration = True

    release_notes = ""
    changelog_url = f"https://github.com/{os.environ['GITHUB_REPOSITORY']}/releases/tag/{tag}"
    async with aiohttp.ClientSession() as session:
        async with session.get(changelog_url) as response:
            if response.status == 200:
                release_notes = await response.text()

    version_data = {
        "code": code,
        "migration": migration,
        "minSdk": MIN_SDK,
        "releaseNotes": release_notes,
        "version": tag,
        "appId": APP_ID
    }
    write_to_file("version.json", version_data)

    return True