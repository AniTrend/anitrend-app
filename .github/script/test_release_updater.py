import unittest
from unittest import mock
from unittest.mock import patch

from release_updater import generate_release_files


class TestGenerateReleaseFiles(unittest.TestCase):
    @patch("release_updater.get_latest_release_tag", return_value="1.9.0")
    @patch("release_updater.get_release_notes", return_value="Dummy release notes")
    @patch("release_updater.get_existing_version_json", return_value=None)
    @patch("release_updater.get_existing_release_properties", return_value=None)
    @patch("release_updater.get_current_timestamp", return_value="2022-03-19T12:34:56.789Z")
    @patch("release_updater.get_code_from_tag", return_value=1009000000)
    @patch("release_updater.get_version_from_tag", return_value="1.9.0")
    @patch("release_updater.get_latest_commit_sha", return_value="abcdef123456")
    @patch("release_updater.get_branch_name", return_value="main")
    @patch("release_updater.get_commit_messages", return_value=["commit message"])
    @patch("release_updater.get_commit_authors", return_value=["John Doe"])
    @patch("release_updater.get_modified_files", return_value=["file1.py"])
    @patch("release_updater.get_added_files", return_value=["file2.py"])
    @patch("release_updater.get_removed_files", return_value=["file3.py"])
    @patch("release_updater.get_changed_files", return_value=["file1.py", "file2.py"])
    @patch("release_updater.write_to_file")
    async def test_generate_files_with_migration(self, mock_write, *mocks):
        await generate_release_files()
        mock_write.assert_has_calls(
            [
                mock.call(
                    "release.properties",
                    "version=1.9.0\ncode=1009000000\ncommit=abcdef123456\nbranch=main\ntimestamp=2022-03-19T12:34:56.789Z\n",
                ),
                mock.call(
                    "version.json",
                    '{"code": 1009000000, "migration": true, "minSdk": 17, "releaseNotes": "Dummy release notes", '
                    '"version": "1.9.0", "appId": "com.domain.app"}\n',
                ),
            ]
        )

    @patch("release_updater.get_latest_release_tag", return_value="1.10.0")
    @patch("release_updater.get_release_notes", return_value="Dummy release notes")
    @patch("release_updater.get_existing_version_json", return_value='{"code": 1009000000, "migration": true, "minSdk": 17, "releaseNotes": "Dummy release notes", "version": "1.9.0", "appId": "com.domain.app"}\n')
    @patch("release_updater.get_existing_release_properties", return_value="version=1.9.0\ncode=1009000000\ncommit=abcdef123456\nbranch=main\ntimestamp=2022-03-19T12:34:56.789Z\n")
    @patch("release_updater.get_current_timestamp", return_value="2022-03-19T12:34:56.789Z")
    @patch("release_updater.get_code_from_tag", return_value=1010000000)
    @patch("release_updater.get_version_from_tag", return_value="1.10.0")
    @patch("release_updater.get_latest_commit_sha", return_value="abcdef123456")
    @patch("release_updater.get_branch_name", return_value="main")
    @patch("release_updater.get_commit_messages", return_value=["commit message"])
    @patch("release_updater.get_commit_authors", return_value=["John Doe"])
    @patch("release_updater.get_modified_files", return_value=["file1.py"])
    @patch("release_updater.get_added_files", return_value=["file2.py"])
    @patch("release_updater.get_removed_files", return_value=["file3.py"])
    @patch("release_updater.get_changed_files", return_value=["file1.py", "file2.py"])
    @patch("release_updater.write_to_file")
    async def test_generate_files_with_new_release(self, mock_write, *mocks):
        await generate_release_files()
        mock_write.assert_has_calls(
            [
                mock.call(
                    "release.properties",
                    "version=1.10.0\ncode=1010000000\ncommit=abcdef123456\nbranch=main\ntimestamp=2022-03-19T12:34:56.789Z\n",
                ),
                mock.call(
                    "version.json",
                    '{"code": 1010000000, "migration": true, "minSdk": 17, "releaseNotes": "Dummy release notes", '
                    '"version": "1.10.0", "appId": "com.domain.app"}\n',
                ),
            ]
        )
