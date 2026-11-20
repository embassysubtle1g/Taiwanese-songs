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
