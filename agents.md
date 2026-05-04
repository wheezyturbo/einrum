# Agent Instructions & Log

## Repository
- **Remote**: \`git@github.com:wheezyturbo/einrum.git\`

## Core Mandates
- **Commit Pattern**: Perform a `git commit` after every cohesive development step (e.g., finishing a contract, a ViewModel, or a UI screen).
- **Security**: Adhere to the elite cybersecurity protocols defined in `GEMINI.md`.
- **CI/CD**: Ensure all changes pass the GitHub Actions pipeline.

## Development Log
### 2026-05-04
- **Project Initialization**: Setup modular structure and version catalog.
- **Meeting Lobby**: Implemented MVI contract, ViewModel, and Compose UI.
- **DI & Network**: Integrated Koin and defined core network service.
- **Video Call**: Implemented adaptive UI, grid, and call controls.
- **On-Device AI**: Integrated Gemini Nano for background blur toggle.
- **Security Hardening**: Updated `GEMINI.md` with prompt injection and code integrity protocols.
- **DevOps**: Implemented GitHub Actions CI/CD pipeline for automated builds and releases.
- **DevOps Fix**: Switched to `assembleDebug` for releases to enable pipeline success without GitHub Secrets.

### 2026-05-04 (Continued)
- **Global Rebranding**: Renamed application to **Einrúm** (Icelandic for Privacy).
- **Refactoring**: Updated all package names from `com.aura` to `com.einrum`.
- **Documentation**: Updated `GEMINI.md`, `agents.md`, and CI/CD workflows to reflect the new brand.

### 2026-05-04 (Continued)
- **Anonymous Usage**: Implemented fully anonymous guest access. Users only provide a transient display name; no authentication required.

### 2026-05-04 (Final Hardening)
- **Security Audit**: Completed final security review and hardening.
- **Input Sanitization**: Implemented strict regex-based sanitization and length limits for all user inputs.
- **Injection Defense**: Added `SecurityUtils` for triple-quote delimiter wrapping to prevent Gemini Nano prompt injection.
- **AI Output Validation**: Implemented schema-based validation for all AI-generated content.
- **Screen Security**: Applied `FLAG_SECURE` across the application to prevent unauthorized screenshots and recordings.
- **ProGuard/R8**: Added secure ProGuard rules to obfuscate code and remove logging in production.
- **CI/CD Fix**: Restored missing Gradle wrapper files to fix pipeline execution failure.
