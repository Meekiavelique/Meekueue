![image](https://github.com/user-attachments/assets/b019deb9-64f9-402b-a5a2-a2c9a90e14e5)
## Features

- Queue management for players joining the main server
- Whitelist system for priority access
- Admin commands to control the queue
- Real-time queue position updates for players
- Configurable retry delay for connection attempts
### TODO
- Punishing players in the queue, slurs in the waiting server make them down the queue for example
- Adding proxied server wide PlaceHolders
- Custom screens using ItemsAdder to show black screen with the position queue
- A configuration file to and a reload command to change values without editing the code
- Queue list in order in a Minecraft
## Installation

1. Download the latest version of Meekueue from the [releases page](https://github.com/Meekiavelique/Meekueue/releases).
2. Place the JAR file in your Velocity server's `plugins` folder. (Still didn't made releases)
3. Restart your Velocity server.

## Usage

### Player Queue

Players will automatically be added to the queue when they attempt to join the waiting server. They will receive updates about their position in the queue.

### Admin Commands

- `/queue enable` - Enable the queue system
- `/queue disable` - Disable the queue system
- `/queue whitelist add <player>` - Add a player to the whitelist (meekueue.admin)
- `/queue whitelist remove <player>` - Remove a player from the whitelist (meekueue.admin)

### Permissions

- `meekueue.admin` - Required to use admin commands

## Configuration

Currently, the plugin does not have a configuration file. Future versions may include customizable options.

## Building from Source

To build Meekueue from source:

1. Clone the repository
2. Navigate to the project directory
3. Run `./gradlew build` (or `gradlew.bat build` on Windows)
4. The compiled JAR will be in the `build/libs` directory

Or using maven

`mvn clean package`

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Support

For support, please open an issue on the [GitHub repository](https://github.com/Meekiavelique/Meekueue/issues).
Or you are free to dm in discord : `@billetde20`

## Authors

- Meekiavelique

## Acknowledgments

- Thanks to the Paper team for their excellent proxy server software.
