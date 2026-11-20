// Google Apps Script Web App API
const API_URL =
  "https://script.google.com/macros/s/AKfycbxoFMPM8tuVUIMuzEGGPC7_goxO_OBhTCe3oUSNcw3zP7sav9-X5xygVx0DjyycGRi_/exec";

// 使用 JSONP 呼叫 Apps Script，避免 GitHub Pages 跨網域讀取 JSON 時的 CORS 問題。
function callApi(action) {
  return new Promise((resolve, reject) => {
    const callbackName =
      "taiwaneseSongsCallback_" +
      Date.now() +
      "_" +
      Math.random().toString(36).slice(2);

    const script = document.createElement("script");
    let timeoutId;

    function cleanup() {
      if (timeoutId) {
        clearTimeout(timeoutId);
      }

      delete window[callbackName];
      script.remove();
    }

    window[callbackName] = function(response) {
      cleanup();

      if (!response || response.success !== true) {
        reject(new Error(
          response && response.error
            ? response.error
            : "API 回傳資料格式不正確。"
        ));
        return;
      }

      resolve(response.data);
    };

    script.onerror = function() {
      cleanup();
      reject(new Error("無法連線至歌曲資料 API。"));
    };

    timeoutId = setTimeout(() => {
      cleanup();
      reject(new Error("API 回應逾時，請稍後再試。"));
    }, 15000);

    const params = new URLSearchParams({
      action: action,
      callback: callbackName
    });

    script.src = API_URL + "?" + params.toString();
    document.head.appendChild(script);
  });
}


// 儲存所有歌曲資料
let allSongs = [];

// 紀錄歌曲是否載入完成
let songsLoaded = false;

// 記錄使用者目前點選的歌手
let selectedArtistID = null;
let selectedArtistName = null;

document.addEventListener("DOMContentLoaded", function() {

  document.getElementById("backButton").style.display = "none";

  // 載入歌手資料
  callApi("getArtists")
    .then(showArtists)
    .catch(function(error) {
      document.getElementById("loading").style.display = "none";
      console.error("歌手載入失敗", error);
      alert("歌手資料載入失敗，請重新整理頁面");
    });

  // 背景載入歌曲
  callApi("getSongs")
    .then(function(songs) {

      // 儲存歌曲資料
      allSongs = songs;

      // 標記歌曲已載入完成
      songsLoaded = true;

      // 關閉 Loading
      document.getElementById("loading").style.display = "none";

      // 如果使用者已經點了歌手
      if (selectedArtistID !== null) {

        const artistSongs =
          allSongs.filter(
            song => song.ArtistID == selectedArtistID
          );

        displaySongs(
          artistSongs,
          selectedArtistName
        );
      }

    })
    .catch(function(error) {

      // 載入失敗也要關閉 Loading
      document.getElementById("loading").style.display = "none";

      console.error("歌曲載入失敗", error);

      alert("歌曲資料載入失敗，請重新整理頁面");

    });

});


function showArtists(artists) {

  const container =
    document.getElementById("artistList");

  container.innerHTML = "";

  artists.forEach(artist => {

    const div = document.createElement("div");

    div.className = "artist";

    div.innerHTML = `
      <img src="${artist.ImageURL}">
      <div class="artist-name">
        ${artist.ArtistName}
      </div>
    `;

    div.onclick = function() {
      showSongs(artist.ArtistID, artist.ArtistName);
    };

    container.appendChild(div);

  });

}


function showSongs(artistID, artistName) {

  // 顯示返回按鈕
  document.getElementById("backButton").style.display = "block";

  // 記住使用者點選的歌手
  selectedArtistID = artistID;
  selectedArtistName = artistName;

  // 歌曲還沒載入完成
  if (!songsLoaded) {

    // 顯示 Loading
    document.getElementById("loading").style.display = "flex";

    return;
  }

  // 從已經載入的歌曲資料中篩選
  const artistSongs =
    allSongs.filter(
      song => song.ArtistID == artistID
    );

  // 顯示歌曲
  displaySongs(
    artistSongs,
    artistName
  );

}


function displaySongs(songs, artistName) {

  const artistList =
    document.getElementById("artistList");

  const songList =
    document.getElementById("songList");

  const songsContainer =
    document.getElementById("songs");

  const pageTitle =
    document.getElementById("pageTitle");

  artistList.style.display = "none";
  songList.style.display = "block";

  pageTitle.textContent = artistName;

  songsContainer.innerHTML = "";

  songs.forEach(song => {

    const div = document.createElement("div");

    div.className = "song";

    div.textContent = song.SongName;

    div.onclick = function() {
      playSong(song.YouTubeURL);
    };

    songsContainer.appendChild(div);

  });

  // 重新觸發動畫
  songList.classList.remove("page-enter");
  void songList.offsetWidth;
  songList.classList.add("page-enter");
}


// 返回歌手列表
function showArtistsPage() {

  document.getElementById("backButton").style.display = "none";

  const songList =
    document.getElementById("songList");

  const artistList =
    document.getElementById("artistList");

  songList.style.display = "none";
  artistList.style.display = "flex";

  document.getElementById("pageTitle").textContent = "歌手";

  artistList.classList.remove("page-enter");
  void artistList.offsetWidth;
  artistList.classList.add("page-enter");
}


function playSong(url) {

  window.open(url, "_blank");

}
