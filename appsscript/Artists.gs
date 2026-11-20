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
