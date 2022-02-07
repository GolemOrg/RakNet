# Overview
Given the inaccuracies of the main [documentation](https://wiki.vg/Raknet_Protocol) for RakNet, this is an attempt
to provide a more accurate and complete description of the protocol.

# Data Types

## Basic Types

| Type                         | Size (in bytes) | Range                          | Documentation                                               |
|------------------------------|-----------------|--------------------------------|-------------------------------------------------------------|
| Byte                         | 1               | 0 - 255                        |                                                             |
| Boolean                      | 1               | 0 - 1                          | A one-byte value that represents true (0x1) or false (0x0). |
| Short                        | 2               | -32,678 - 32,677               |                                                             |
| Unsigned Short               | 2               | 0 - 65,535                     |                                                             |
| Unsigned Triad LE (uint24le) | 3               | 0 - 16,777,215                 | This value is encoded as little endian.                     |
| Integer                      | 4               | -2,147,483,648 - 2,147,483,647 | A signed 32-bit integer.                                    |
| Long                         | 8               | -2^63 to 2^63-1                | A signed 64-bit integer.                                    |

## Advanced Types

### String
In RakNet, a string is encoded as an unsigned short followed by the string bytes.

As an example, "Hello, World!" is encoded as the following:

| Encode Order            | Example Value |
|-------------------------|---------------|
| Length (Unsigned Short) | 13            |
| Bytes                   | Hello, World! |

### Address

For IP addresses, encoding depends on the address family.

For IPv4 addresses, the address is encoded as the following:

| Encode Order   | Type           | Example Value    | Documentation                                                                            |
|----------------|----------------|------------------|------------------------------------------------------------------------------------------|
| Address Family | Byte           | 4                | IPv4                                                                                     |
| Bytes          | Byte Array (4) | [192, 168, 0, 1] | Each part of the address is inverted using bitwise `~` and then masked with bitwise `&`. |
| Port           | Unsigned Short | 19132            |                                                                                          |

For IPv6 addresses, the address is encoded as the following:

| Encode Order   | Type                  | Example Value                                    | Documentation                                           |
|----------------|-----------------------|--------------------------------------------------|---------------------------------------------------------|
| Address Family | Byte                  | 6                                                | IPv6                                                    |
| AF_INET6       | Short (Little Endian) | 10                                               | AF (Address Family), INET6 (IPv6)                       |
| Port           | Short                 | 19132                                            |                                                         |
| Flow Info      | Int                   | 0                                                | A part of the IPv6 protocol. `0` works fine as a value. |
| Address        | Byte Array (16)       | [0000, 0000, 0000, 0000, 0000, 0000, 0000, 0001] |                                                         |
| Scope ID       | Int                   | 0                                                | A part of the IPv6 protocol. `0` works fine as a value. |

### Magic

In RakNet, Magic, also known as OFFLINE_MESSAGE_DATA_ID in the original source, is a set of bytes that is
used as a way to distinguish between offline messages and the rest of the protocol.
The hex bytes for Magic are:
`0x00,0xFF,0xFF,0x00,0xFE,0xFE,0xFE,0xFE,0xFD,0xFD,0xFD,0xFD,0x12,0x34,0x56,0x78`

# Packet Types
The following is a list of all the packet types in the protocol.

## Offline Messages
These are the packets that are sent to/from the server when the client has not
established a connection with the server.

### Unconnected Ping (`0x01`) | Client → Server
This packet is sent to the server as a way to request information about the server.

| Field Name  | Field Type | Documentation                                      |
|-------------|------------|----------------------------------------------------|
| Timestamp   | Long       | The current timestamp (in milliseconds)            |
| Magic       | Magic      | See above in "Data Types" for more about this type |
| Client GUID | Long       | A unique identifier sent by the client             |

### Unconnected Pong (`0x1C`) | Server → Client
This packet is sent to the client as a way to respond to the Unconnected Ping.

| Field Name  | Field Type | Documentation                                                   |
|-------------|------------|-----------------------------------------------------------------|
| Timestamp   | Long       | The timestamp from the UnconnectedPing sent by the client.      |
| Server GUID | Long       | A unique identifier sent by the server                          |
| Magic       | Magic      | See above in "Data Types" for more about this type              |
| Server Name | String     | In Bedrock Edition, this is used as a way to display the MOTD.  |

#### Server Name Format
In Bedrock Edition, the format for the server name is made up of the fields down below. Each
field is separated by the `;` character.

| Name                | Type       | Documentation                                                   |
|---------------------|------------|-----------------------------------------------------------------|
| Header              | String     | MCPE (for Bedrock Edition) or MCEE (for Education Edition)      |
| MOTD                | String     | The main message seen by the client                             |
| Protocol Version    | Int        | The game protocol version (e.g., 475)                           |
| Game Version        | String     | The game version (e.g., v1.18.0)                                |
| Player Count        | Int        | The number of players currently online                          |
| Max Player Count    | Int        | The maximum amount of players that can be on the server         |
| Server GUID         | Long       | The unique identifier sent by the server                        |
| Sub-MOTD            | String     | The secondary line (not seen by the client). Length must be > 0 |
| Game-mode           | String     | Creative, Survival, Adventure, etc.                             |
| Game-mode (Numeric) | Int        | The only valid values for this are: 0, 1                        |
| Port (IPv4)         | Int        | The port used by the server when connecting through IPv4        |
| Port (IPv6)         | Int        | The port used by the server when connecting through IPv6        |

### Open Connection Request 1 (`0x05`) | Client → Server
This is the initial packet sent when the client wants to establish a connection with the server.

| Field Name                   | Field Type   | Documentation                                                                                                       |
|------------------------------|--------------|---------------------------------------------------------------------------------------------------------------------|
| Magic                        | Magic        | See above in "Data Types" for more about this type                                                                  |
| Protocol Version             | Byte         | The current RakNet version (10 for Bedrock Edition)                                                                 |
| Maximum Transfer Units (MTU) | Zero padding | The MTU size for the client consists of the entire buffer, but the padding is used as a way to get it to that value |

### Open Connection Reply 1 (`0x06`) | Server → Client
This packet is sent in response to the OpenConnectionRequest1 packet.

| Field Name   | Field Type | Documentation                                                                          |
|--------------|------------|----------------------------------------------------------------------------------------|
| Magic        | Magic      | See above in "Data Types" for more about this type                                     |
| Server GUID  | Long       | The unique identifier sent by the server                                               |
| Use Security | Boolean    | If using for Bedrock Edition, this field must be set to false                          |
| MTU Size     | Short      | The MTU size from OpenConnectionRequest1 + 20 bytes (IP header) + 8 bytes (UDP header) |

### Open Connection Request 2 (`0x07`) | Client → Server
This is the second packet sent when the client wants to establish a connection with the server.

| Field Name     | Field Type | Documentation                                                      |
|----------------|------------|--------------------------------------------------------------------|
| Magic          | Magic      | See above in "Data Types" for more about this type                 |
| Server Address | Address    | The IP address of the server                                       |
| MTU Size       | Short      | The finalized MTU size sent by the client                          |
| Client GUID    | Long       | The unique identifier sent by the client                           |


### Open Connection Reply 2 (`0x08`) | Server → Client
This packet is sent in response to the OpenConnectionRequest2 packet.

| Field Name         | Field Type | Documentation                                      |
|--------------------|------------|----------------------------------------------------|
| Magic              | Magic      | See above in "Data Types" for more about this type |
| Server GUID        | Long       | The unique identifier sent by the server           |
| Client Address     | Address    | The IP address of the client connecting            |
| MTU Size           | Short      | The final size of the MTU                          |
| Encryption Enabled | Boolean    | Whether or not encryption will be used             |

### Incompatible Protocol Version (`0x19`) | Server → Client
This packet is sent when the client is attempting to connect to a server that is running on a different RakNet version.

| Field Name       | Field Type | Documentation                                      |
|------------------|------------|----------------------------------------------------|
| Protocol Version | Byte       | The protocol version of the server                 |
| Magic            | Magic      | See above in "Data Types" for more about this type |
| Server GUID      | Long       | The unique identifier sent by the server           |


## Connected Packets

In the RakNet protocol, all connected packets are contained inside a container format called a `Datagram`.

### Datagram


### Frame / Encapsulated Packet


### Connected Ping (`0x00`) | Client → Server or Server → Client
This packet is usually sent by the client to the server, but can also be sent by the server to the client.
