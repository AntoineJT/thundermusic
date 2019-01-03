import idb from "idb";

const db = idb.open("thundermusic", 2, upgradeDb => {
  /* eslint-disable no-fallthrough */
  switch (upgradeDb.oldVersion) {
    case 0: {
      const musics = upgradeDb.createObjectStore("musics", { keyPath: "id" });
      const playlists = upgradeDb.createObjectStore("playlists", {
        keyPath: "id"
      });

      const oldMusics = localStorage.getItem("musics");
      const oldPlaylists = localStorage.getItem("playlists");

      if (oldMusics)
        for (const music of JSON.parse(oldMusics)) musics.put(music);
      if (oldPlaylists)
        for (const playlist of JSON.parse(oldPlaylists))
          playlists.put(playlist);
    }
    case 1:
      upgradeDb.createObjectStore("settings");
  }
});

db.catch(alert);

export async function addMusic(music) {
  const tx = (await db).transaction("musics", "readwrite");
  tx.objectStore("musics").add(music);
  return tx.complete;
}

export async function deleteMusic(id) {
  const tx = (await db).transaction("musics", "readwrite");
  tx.objectStore("musics").delete(id);
  return tx.complete;
}

export async function updateMusic(music) {
  const tx = (await db).transaction("musics", "readwrite");
  tx.objectStore("musics").put(music);
  return tx.complete;
}

export async function getMusics() {
  const tx = (await db).transaction("musics");
  return tx.objectStore("musics").getAll();
}

export async function addPlaylist(playlist) {
  const tx = (await db).transaction("playlists", "readwrite");
  tx.objectStore("playlists").add(playlist);
  return tx.complete;
}

export async function getPlaylists() {
  const tx = (await db).transaction("playlists");
  return tx.objectStore("playlists").getAll();
}

export async function setSetting(key, value) {
  const tx = (await db).transaction("settings", "readwrite");
  tx.objectStore("settings").put(value, key);
  return tx.complete;
}

export async function getSettings() {
  const tx = (await db).transaction("settings");
  let cursor = await tx.objectStore("settings").openCursor();
  const result = [];
  while (cursor) {
    result.push([cursor.key, cursor.value]);
    cursor = await cursor.continue();
  }
  return result;
}
