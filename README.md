<p align="center">
  <img src="https://github.com/IchinichiQ/Kriegspiel/assets/47754830/6b9aeb7d-04c1-4166-8eef-b13e6b371e96" width="150px" height="150px" alt="Logo">
</p>
<h1 align="center">Kriegspiel</h1>

This is a Java version of a turn-based board game Kriegspiel. It was made to practice OOP paradigm, SOLID principles, Client-Server architecture and network protocols.

## Features

* Client-Server architecture.
* Support of local offline games and online games.
* The game SDK has been developed on the basis of which the client and server are running.
* Swing responsive UI.
* Developed using OOP paradigm and SOLID principles.

## Application Architecture


When developing the SDK, I kept in mind the possibility of modifying the game without directly modifying the SDK itself. To achieve this, all the core game objects are abstracted, so the SDK only interacts with abstract objects.  

To ensure that the state of game objects cannot be tampered with from the outside, encapsulation is applied. For example, all player interactions with the game occur through the Game interface, which encapsulates the game objects. The Game interface is not a "God class" but only implements methods that players interact with.

The user interface utilizes the GameEventListener, which reports all game events. Online games are also implemented based on it.

Online games use a Client-Server architecture based on sockets. Data is serialized and deserialized through the "Protocol" interface. The server employs a simple matchmaking system that creates a new room if there are no available ones, and if a room already exists, it adds a second player and starts the game. Therefore, the server supports an unlimited number of players.

SDK class diagram:  
![Class diagram](https://github.com/IchinichiQ/Kriegspiel/assets/47754830/0657bffe-76bb-4821-876d-f31cb5a32e26)

## Game rules

The game is played by two players on a game board of 500 squares arranged in rows of 20 by 25. The board is divided into a northern territory and a southern territory, each with a single mountain range of nine squares, a mountain pass, two arsenals, and three fortresses. If a unit stands on a square with a fortress, it gains 4 bonus defense points. Likewise, cells with mountain pass give 2 defense points.  

Each player has a communication network that must be maintained and protected. The network is powered by the player's two immobile arsenals, which radiate lines of communication vertically, horizontally, and at 45º diagonals. Each player also has two mobile relay units, which reflect any line of communication aimed at them. Friendly units must remain connected to the network, else risk being captured. Lines of communication can be severed by the enemy and thus are crucial to strategy.  

The object of the game is to destroy the opponent, either by eliminating all its forces, or by destroying its two arsenals.  


## How to run

You can build it from source or download pre-built version from [releases page](https://github.com/IchinichiQ/Kriegspiel/releases).

## Gallery
Game field:  
![Game field](https://github.com/IchinichiQ/Kriegspiel/assets/47754830/2881ee39-e8df-41e5-835e-c3c7bbdc2ff7)

Gameplay video:  

https://github.com/IchinichiQ/Kriegspiel/assets/47754830/befc5870-cc53-4693-b66a-ceb7ae321b9c

Online game demonstration:  

https://github.com/IchinichiQ/Kriegspiel/assets/47754830/11cbda55-9597-4937-a384-41b0b5eca151
