/**
 * API 入口
 *
 * GET:
 *   ?action=getArtists
 *   ?action=getSongs
 *   ?action=getArtists&callback=handleApiResponse
 *
 * POST:
 *   JSON body: {"action":"getArtists"}
 *
 * 公開唯讀 API。不要在此加入未經驗證的資料寫入功能。
 */

function doGet(e) {
  const params = (e && e.parameter) ? e.parameter : {};
  const action = params.action || "";
  const callback = params.callback || "";

  return executeApiAction_(action, callback);
}

function doPost(e) {
  let request;

  try {
    const body = e && e.postData && e.postData.contents
      ? e.postData.contents
      : "";

    request = body ? JSON.parse(body) : {};
  } catch (error) {
    return createJsonOutput_({
      success: false,
      data: null,
      error: "Request body 必須是有效的 JSON。"
    });
  }

  return executeApiAction_(request.action || "", "");
}

/**
 * 執行 API 動作，統一包裝成功與錯誤回應。
 */
function executeApiAction_(action, callback) {
  try {
    let data;

    switch (action) {
      case "getArtists":
        data = getArtists();
        break;

      case "getSongs":
        data = getSongs();
        break;

      default:
        return createApiOutput_({
          success: false,
          data: null,
          error: "不支援的 action。"
        }, callback);
    }

    return createApiOutput_({
      success: true,
      data: data,
      error: null
    }, callback);

  } catch (error) {
    console.error("API 執行失敗：", error);

    return createApiOutput_({
      success: false,
      data: null,
      error: "伺服器處理資料時發生錯誤。"
    }, callback);
  }
}

/**
 * 有 callback 時回傳 JSONP；沒有 callback 時回傳純 JSON。
 */
function createApiOutput_(payload, callback) {
  if (callback) {
    // 只允許單一 JavaScript 函式名稱，避免 callback 注入任意程式碼。
    const validCallback = /^[A-Za-z_$][0-9A-Za-z_$]*$/.test(callback);

    if (!validCallback) {
      return createJsonOutput_({
        success: false,
        data: null,
        error: "callback 格式不正確。"
      });
    }

    // 避免資料中的特殊字元被解讀成可執行程式碼。
    const json = JSON.stringify(payload)
      .replace(/</g, "\\u003c")
      .replace(/>/g, "\\u003e")
      .replace(/&/g, "\\u0026");

    return ContentService
      .createTextOutput(callback + "(" + json + ");")
      .setMimeType(ContentService.MimeType.JAVASCRIPT);
  }

  return createJsonOutput_(payload);
}

/**
 * 回傳 JSON 格式。
 */
function createJsonOutput_(payload) {
  return ContentService
    .createTextOutput(JSON.stringify(payload))
    .setMimeType(ContentService.MimeType.JSON);
}
