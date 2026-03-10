# Discord Bot

A feature-rich Discord bot built in Java that provides music playback, text-to-speech synthesis, AI-powered music recommendations, and various utility commands.

## 📋 Table of Contents

- [Features](#features)
- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Installation](#installation)
- [Configuration](#configuration)
- [Usage](#usage)
- [API Dependencies](#api-dependencies)
- [Architecture](#architecture)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)

## ✨ Features

### Audio & Music Playback
- **Music Queue Management**: Play songs from YouTube with an intelligent queue system
- **Playback Controls**: Play, pause, resume, skip, and stop functionality
- **Mood-Based Recommendations**: AI-powered song recommendations based on mood or situation
- **Local Audio Support**: Play audio files from the local filesystem
- **Queue Visualization**: Display current music queue to users

### Voice & Text-to-Speech
- **Text-to-Speech Synthesis**: Convert text to speech using AWS Polly with neural voices
- **Voice Channel Management**: Connect to and disconnect from Discord voice channels
- **Automatic Disconnect**: Bot automatically leaves when it's alone in a voice channel

### AI Integration
- **DeepSeek AI Provider**: Real-time interaction with the DeepSeek API for song recommendations
- **Song Verification**: AI ensures only real existing songs are recommended
- **JSON Response Handling**: Structured responses for batch song lookups

### Utility Commands
- **Ping Command**: Check bot responsiveness
- **Say Command**: Make the bot repeat custom messages
- **Dynamic Command Registration**: Global slash commands registered on startup

## 🔧 Prerequisites

### System Requirements
- **Java**: Version 24 or higher (specified in `pom.xml`)
- **Operating System**: Windows, macOS, or Linux
- **Build Tool**: Maven 3.6+

### External Tools (Auto-downloaded on first run)
- **yt-dlp**: YouTube video downloader (handles audio extraction)
- **FFmpeg**: Audio processing and conversion utility
- **Python/Jython**: Required for yt-dlp operations

### API Keys Required
You will need the following API keys stored in `secrets.json`:

1. **Discord Bot Token** - [Create at Discord Developer Portal](https://discord.com/developers/applications)
2. **YouTube Data API Key** - [Google Cloud Console](https://console.cloud.google.com/)
3. **DeepSeek API Key** - [DeepSeek Platform](https://platform.deepseek.com/)
4. **AWS Credentials** (Access Key & Secret Key) - For Amazon Polly text-to-speech
   - Ensure your AWS account has Polly permissions
