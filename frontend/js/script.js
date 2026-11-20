// 儲存所有歌曲資料
let allSongs = [];

// 紀錄歌曲是否載入完成
let songsLoaded = false;

// 記錄使用者目前點選的歌手
let selectedArtistID = null;
let selectedArtistName = null;

document.addEventListener("DOMContentLoaded", function() {

  document.getElementById("backButton").style.display = "none";

  // 暫時保留原本 Apps Script 呼叫方式。
  // GitHub Pages 版本尚未接上 JSON API；完成 API 後會在下一階段替換這裡。
  google.script.run
    .withSuccessHandler(showArtists)
    .getArtists();

  // 背景載入歌曲
  google.script.run
    .withSuccessHandler(function(songs) {

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
    .withFailureHandler(function(error) {

      // 載入失敗也要關閉 Loading
      document.getElementById("loading").style.display = "none";

      console.error("歌曲載入失敗", error);

      alert("歌曲資料載入失敗，請重新整理頁面");

    })
    .getSongs();

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
