CREATE TABLE worlds
(
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    identifier TEXT NOT NULL UNIQUE
);

CREATE TABLE types
(
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    identifier TEXT NOT NULL UNIQUE
);
INSERT INTO types (id, identifier) VALUES (0, 'minecraft:air');

CREATE TABLE blocks_data
(
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    block_data TEXT NOT NULL UNIQUE
);

CREATE TABLE users
(
    id       INTEGER PRIMARY KEY AUTOINCREMENT,
    uniqueId TEXT NOT NULL UNIQUE,
    nickname TEXT NOT NULL
);

CREATE TABLE block_placements
(
    id                         INTEGER PRIMARY KEY AUTOINCREMENT,
    userId                     INTEGER NOT NULL,
    timestamp                  INTEGER NOT NULL,
    x                          INTEGER NOT NULL,
    y                          INTEGER NOT NULL,
    z                          INTEGER NOT NULL,
    worldId                    INTEGER NOT NULL,
    placedBlockId              INTEGER NOT NULL,
    placedBlockDataId          INTEGER,
    placedBlockNBT             BLOB,
    placedBlockContainerData   INTEGER,
    replacedBlockId            INTEGER NOT NULL,
    replacedBlockDataId        INTEGER,
    replacedBlockNBT           BLOB,
    replacedBlockContainerData INTEGER,

    FOREIGN KEY (userId) REFERENCES users (id),
    FOREIGN KEY (worldId) REFERENCES worlds (id),
    FOREIGN KEY (placedBlockId) REFERENCES types (id),
    FOREIGN KEY (placedBlockDataId) REFERENCES blocks_data (id),
    FOREIGN KEY (replacedBlockId) REFERENCES types (id),
    FOREIGN KEY (replacedBlockDataId) REFERENCES blocks_data (id)
);

CREATE TABLE containers_items_data
(
    id      INTEGER NOT NULL,
    itemId  INTEGER NOT NULL,
    amount  INTEGER NOT NULL,
    slot    INTEGER NOT NULL,
    itemNBT BLOB
);