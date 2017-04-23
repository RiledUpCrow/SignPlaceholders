# SignPlaceholders

Spigot plugin which allows displaying [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) placeholders on signs.

## Usage

To register a sign as a dynamic placeholder display look at it and type `/sph add <text>`. This will put `<text>` on the sign and replace all placeholders. To split text into multiple lines use `|` character. For example this command: `/sph add | Score: | %some_score%` will add a sign with `Score:` text on the second line and the `%some_score%` placeholder on the third line.

To unregister a sign look at it and type `/sph del` or simply break it.

To reload the plugin use `/sph reload`. It's only needed if you edit the _config.yml_ file manually. Please don't do that though, it may cause errors if you make a mistake.

To change the update interval of signs use `/sph interval <number>`, where the number is amount of ticks between each update.

To change the range around players in which the signs are updated use `/sph range <number>` where the number is a radius in blocks.