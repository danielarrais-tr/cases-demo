/*
  Utilitários para simular beginOAuth2/completeOAuth2 com popup.
*/

function generateRandomString(win) {
  const w = win || window;
  const arr = new Uint8Array(16);
  if (w.crypto && w.crypto.getRandomValues) {
    w.crypto.getRandomValues(arr);
  } else {
    for (let i = 0; i < arr.length; i += 1) {
      arr[i] = Math.floor(Math.random() * 256);
    }
  }
  return Array.from(arr).map((b) => b.toString(16).padStart(2, '0')).join('');
}

function generateFakeToken() {
  return 'tok_' + generateRandomString(window) + '_' + Date.now().toString(36);
}

function decodeQueryString(qs) {
  if (!qs) return {};
  const out = {};
  for (const part of qs.split('&')) {
    if (!part) continue;
    const [k, v] = part.split('=');
    out[decodeURIComponent(k)] = decodeURIComponent(v || '');
  }
  return out;
}

function toQueryString(params) {
  const usp = new URLSearchParams();
  Object.keys(params).forEach((k) => {
    if (params[k] !== undefined && params[k] !== null) {
      usp.set(k, String(params[k]));
    }
  });
  return usp.toString();
}

function notifyAndClose(detail, clientId) {
  try {
    if (window.opener) {
      window.opener.dispatchEvent(new CustomEvent(`arcgis-rest-js-popup-auth-${clientId}`, { detail }));
    }
  } catch (_) {
    // ignore post-close race conditions
  }
  try {
    window.close();
  } catch (_) {
    // ignore
  }
}

async function beginOAuth2(options) {
  const win = window;
  const {
    portal = win.location.origin,
    provider = 'arcgis',
    clientId,
    expiration = 20160,
    redirectUri,
    popup = true,
    popupWindowFeatures = 'height=400,width=600,menubar=no,location=yes,resizable=yes,scrollbars=yes,status=yes',
    locale = '',
    params,
    style = '',
    pkce = true,
    implicit = false
  } = options || {};

  const stateId = generateRandomString(win);
  const stateStorageKey = `ARCGIS_REST_JS_AUTH_STATE_${clientId}`;
  win.localStorage.setItem(stateStorageKey, stateId);

  const authorizeUrlBase = new URL('auth.html', win.location.href).toString();
  const authorizeUrlParams = {
    client_id: clientId,
    response_type: pkce ? 'code' : 'token',
    expiration,
    redirect_uri: redirectUri,
    state: JSON.stringify({ id: stateId, originalUrl: win.location.href }),
    locale,
    style,
    implicit: String(implicit)
  };

  if (provider !== 'arcgis') {
    authorizeUrlParams.socialLoginProviderName = provider;
    authorizeUrlParams.autoAccountCreateForSocial = true;
  }

  if (pkce) {
    // Simulação de code_challenge
    authorizeUrlParams.code_challenge_method = 'S256';
    authorizeUrlParams.code_challenge = generateRandomString(win).slice(0, 32);
  }

  let authorizeUrl = `${authorizeUrlBase}?${toQueryString(authorizeUrlParams)}`;
  if (params) {
    const extra = typeof params === 'string' ? params : toQueryString(params);
    authorizeUrl += `&${extra}`;
  }

  if (popup) {
    return new Promise((resolve, reject) => {
      function handler(e) {
        try {
          win.removeEventListener(`arcgis-rest-js-popup-auth-${clientId}`, handler);
        } catch (_) {}
        if (e.detail && (e.detail.error || e.detail.errorMessage)) {
          const err = new Error(e.detail.errorMessage || e.detail.error || 'oauth-error');
          reject(err);
          return;
        }
        resolve(e.detail);
      }
      win.addEventListener(`arcgis-rest-js-popup-auth-${clientId}`, handler, { once: true });
      win.open(authorizeUrl, 'oauth-window', popupWindowFeatures);
      win.dispatchEvent(new CustomEvent('arcgis-rest-js-popup-auth-start'));
    });
  }

  win.location.href = authorizeUrl;
  return undefined;
}

// Exporta no escopo global (simples)
window.beginOAuth2 = beginOAuth2;
window.generateRandomString = generateRandomString;
window.decodeQueryString = decodeQueryString;
window.notifyAndClose = notifyAndClose;
window.generateFakeToken = generateFakeToken;

