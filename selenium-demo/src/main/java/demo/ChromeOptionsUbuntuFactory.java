package demo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;

/**
 * ChromeOptions com valores estáticos (Ubuntu-only) prontos para uso.
 *
 * @author Você
 * @version 1.0
 */
public final class ChromeOptionsUbuntuFactory {
	
	// VALORES CRUS
	private static final int SCREEN_WIDTH = 1366;
	private static final int SCREEN_HEIGHT = 768;
	private static final boolean HEADLESS = false;
	private static final boolean INCOGNITO = true;
	private static final boolean BLOCK_NOTIFICATIONS = true;
	private static final boolean ALLOW_PDF_PREVIEW = true;
	private static final String DOWNLOAD_DIR = "/tmp/downloads";
	
	private ChromeOptionsUbuntuFactory() {
	}
	
	/**
	 * Cria ChromeOptions com os mesmos parâmetros essenciais usados no Combinator, fixos para Ubuntu.
	 *
	 * @return ChromeOptions configurado
	 */
	public static ChromeOptions create() {
		ChromeOptions options = new ChromeOptions();
		
		// Estratégias/flags de sessão
		options.setPageLoadStrategy(PageLoadStrategy.NONE);
		options.setUnhandledPromptBehaviour(UnexpectedAlertBehaviour.IGNORE);
		options.setAcceptInsecureCerts(true);
		
		// Argumentos fixos
		options.addArguments("use-fake-ui-for-media-stream");
		// Em Ubuntu NÃO adicionamos "use-fake-device-for-media-stream"
		options.addArguments("no-sandbox");
		options.addArguments("dns-prefetch-disable");
		options.addArguments("disable-translate");
		options.addArguments("use-mock-keychain");
		options.addArguments("password-store=basic");
		options.addArguments("disable-features=MediaRouter");
		options.addArguments("disable-blink-features=BlockCredentialedSubresources");
		options.addArguments("window-size=" + SCREEN_WIDTH + "," + SCREEN_HEIGHT);
		options.addArguments("start-maximized");
		options.addArguments("disable-features=InsecureDownloadWarnings");
		options.addArguments("--disable-search-engine-choice-screen");
		
		if (HEADLESS) {
			options.addArguments("--headless");
			options.addArguments("--disable-gpu");
		}
		if (INCOGNITO) {
			options.addArguments("--incognito");
		}
		
		// Experimental options
		Map<String, Object> perfLoggingPrefs = new HashMap<>();
		perfLoggingPrefs.put("enablePage", false);
		perfLoggingPrefs.put("enableNetwork", true);
		options.setExperimentalOption("perfLoggingPrefs", perfLoggingPrefs);
		options.setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation"));
		
		// Prefs fixos
		Map<String, Object> prefs = new HashMap<>();
		prefs.put("profile.default_content_settings.popups", 0);
		if (BLOCK_NOTIFICATIONS) {
			prefs.put("profile.default_content_setting_values.notifications", 2);
		}
		prefs.put("profile.cookie_controls_mode", 0);
		prefs.put("plugins.always_open_pdf_externally", !ALLOW_PDF_PREVIEW);
		prefs.put("safebrowsing.enabled", true);
		prefs.put("credentials_enable_service", false);
		prefs.put("profile.password_manager_enabled", false);
		
		// Clipboard allow (valor estático)
		Map<String, Object> clipVal = new HashMap<>();
        clipVal.put("last_modified", "0");
        clipVal.put("setting", 1); // 1=enabled, 2=disabled
        Map<String, Object> clipMap = new HashMap<>();
        clipMap.put("[*.],*", clipVal);
        prefs.put("profile.content_settings.exceptions.clipboard", clipMap);
		
		prefs.put("download.default_directory", DOWNLOAD_DIR);
		options.setExperimentalOption("prefs", prefs);
		
		// Logging
		LoggingPreferences logging = new LoggingPreferences();
		logging.enable(LogType.PERFORMANCE, java.util.logging.Level.INFO);
		logging.enable(LogType.BROWSER, java.util.logging.Level.ALL);
		options.setCapability(ChromeOptions.LOGGING_PREFS, logging);
		
		return options;
	}
}