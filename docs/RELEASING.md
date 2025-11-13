# Release Process

This document describes the process for releasing new versions of Parquet Studio.

## Version Numbering

We follow [Semantic Versioning](https://semver.org/):
- **MAJOR** (1.0.0) - Breaking changes
- **MINOR** (1.1.0) - New features, backward compatible
- **PATCH** (1.0.1) - Bug fixes, backward compatible

## Pre-Release Checklist

- [ ] All tests pass
- [ ] Code is reviewed and approved
- [ ] Documentation is updated
- [ ] CHANGELOG.md is updated
- [ ] Version number updated in:
  - `gradle.properties` (`pluginVersion`)
  - `plugin.xml` (if manually set)
- [ ] Build succeeds: `./gradlew clean buildPlugin`

## Release Steps

### 1. Update Version

Edit `gradle.properties`:
```properties
pluginVersion=1.0.1
```

### 2. Update CHANGELOG

Add a new section in `CHANGELOG.md`:
```markdown
## [1.0.1] - 2024-11-13

### Fixed
- Fixed issue with date parsing
- Improved error messages

[1.0.1]: https://github.com/jhordyhuaman/parquet-studio/releases/tag/v1.0.1
```

### 3. Commit Changes

```bash
git add gradle.properties CHANGELOG.md
git commit -m "Release v1.0.1"
git push
```

### 4. Create Tag

```bash
git tag -a v1.0.1 -m "Release v1.0.1"
git push origin v1.0.1
```

### 5. Build Plugin

```bash
./gradlew clean buildPlugin
```

The plugin ZIP will be in `build/distributions/`.

### 6. Create GitHub Release

1. Go to [Releases](https://github.com/jhordyhuaman/parquet-studio/releases)
2. Click "Draft a new release"
3. Select the tag (e.g., `v1.0.1`)
4. Title: `v1.0.1`
5. Description: Copy from CHANGELOG.md
6. Upload the plugin ZIP from `build/distributions/`
7. Click "Publish release"

### 7. Publish to Marketplace

1. Go to [JetBrains Marketplace](https://plugins.jetbrains.com/)
2. Sign in and go to your plugin
3. Click "Update"
4. Upload the new plugin ZIP
5. Fill in release notes (from CHANGELOG)
6. Submit for review

## Automated Release (GitHub Actions)

The `.github/workflows/release.yml` workflow can automate releases:

1. Push a tag starting with `v` (e.g., `v1.0.1`)
2. GitHub Actions will:
   - Build the plugin
   - Create a GitHub release
   - Upload the plugin ZIP

To use:
```bash
git tag v1.0.1
git push origin v1.0.1
```

## Post-Release

- [ ] Verify plugin works in marketplace
- [ ] Monitor for issues
- [ ] Update documentation if needed
- [ ] Announce release (if significant)

## Hotfix Process

For urgent bug fixes:

1. Create branch from latest release tag
2. Fix the issue
3. Follow normal release process
4. Merge back to main

## Rollback

If a release has critical issues:

1. Mark release as deprecated in marketplace
2. Create hotfix release
3. Communicate with users

## Notes

- Always test the plugin ZIP before publishing
- Keep release notes clear and user-friendly
- Follow semantic versioning strictly
- Document breaking changes prominently

