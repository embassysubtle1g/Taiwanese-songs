function doGet() {
  return HtmlService
    .createTemplateFromFile("index")
    .evaluate()
    .setTitle("奶奶音樂");
}

function include(filename) {
  return HtmlService
    .createHtmlOutputFromFile(filename)
    .getContent();
}

// 取得歌手資料
function getArtists() {
  const sheet = SpreadsheetApp
    .getActiveSpreadsheet()
    .getSheetByName("Artists");

  const data = sheet.getDataRange().getValues();
  const headers = data.shift();

  return data.map(row => {
    return {
      ArtistID: row[0],
      ArtistName: row[1],
      ImageURL: row[2]
    };
  });
}

// 取得歌曲資料
function getSongs() {
  const sheet = SpreadsheetApp
    .getActiveSpreadsheet()
    .getSheetByName("Songs");

  const data = sheet.getDataRange().getValues();
  data.shift();

  return data.map(row => {
    return {
      SongID: row[0],
      ArtistID: row[1],
      SongName: row[2],
      YouTubeURL: row[3]
    };
  });
}
