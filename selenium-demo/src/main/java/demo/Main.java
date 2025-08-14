package demo;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.Iterator;
import java.util.Set;

public class Main {
	public static void main(String[] args) throws InterruptedException {
		WebDriverManager.chromedriver().setup();
		WebDriver driver = new ChromeDriver(ChromeOptionsUbuntuFactory.create());

		// Abra uma janela "raiz" e mantenha o foco nela ao iniciar
		ensureOnAnyOpenWindow(driver);

		for (int i = 1; i <= 20; i++) {
			try {
				ensureOnAnyOpenWindow(driver); // <- garante contexto válido no começo do loop

				driver.get("https://danielarrais-tr.github.io/cases-demo/");
				Thread.sleep(500);

				driver.findElement(By.id("btn-login")).click();

				// Troca para a última janela aberta (popup)
				switchToLastWindow(driver);
				Thread.sleep(500);

				driver.findElement(By.id("btn-login-popup")).click(); // clique dentro do popup
				((JavascriptExecutor) driver).executeScript("console.log('Hello from Selenium!');");

				// ... faça o que precisar aqui ...

			} catch (NoSuchWindowException e) {
				System.err.println("[Loop " + i + "] Janela atual não existe mais. Recuperando contexto...");
				ensureOnAnyOpenWindow(driver);
			} catch (Exception e) {
				System.err.println("[Loop " + i + "] Erro: " + e.getMessage());
			} finally {
				// Sempre termine o loop numa janela válida e feche popups sobrando
				try {
					closeAllButFirst(driver);
					ensureOnAnyOpenWindow(driver);
				} catch (Exception ignore) {}
			}
		}

		driver.quit();
	}

	/** Garante foco em qualquer janela ainda aberta. Se não houver, abre uma nova em branco. */
	private static void ensureOnAnyOpenWindow(WebDriver driver) {
		Set<String> handles = driver.getWindowHandles();
		if (handles.isEmpty()) {
			((JavascriptExecutor) driver).executeScript("window.open('about:blank','_blank');");
			handles = driver.getWindowHandles();
		}
		String first = handles.iterator().next();
		driver.switchTo().window(first);
	}

	/** Fecha todas as janelas exceto a primeira (uma "janela raiz" para continuar o teste). */
	private static void closeAllButFirst(WebDriver driver) {
		Set<String> handles = driver.getWindowHandles();
		if (handles.isEmpty()) return;

		Iterator<String> it = handles.iterator();
		String first = it.next(); // mantenha essa aberta
		while (it.hasNext()) {
			String h = it.next();
			try {
				driver.switchTo().window(h);
				driver.close();
			} catch (NoSuchWindowException ignore) {}
		}
		driver.switchTo().window(first);
	}

	/** Troca para a última janela aberta (útil após abrir popup). */
	private static void switchToLastWindow(WebDriver driver) {
		String last = null;
		for (String h : driver.getWindowHandles()) last = h;
		if (last == null) throw new NoSuchWindowException("Nenhuma janela disponível");
		driver.switchTo().window(last);
	}
}
