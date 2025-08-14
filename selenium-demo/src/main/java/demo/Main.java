package demo;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class Main {
    public static void main(String[] args) {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();

        try {
            driver.get("https://danielarrais-tr.github.io/cases-demo/");
			Thread.sleep(5000);

            for (int i = 1; i <= 10; i++) {
				WebElement buttonLogin = driver.findElement(By.id("btn-login"));
				buttonLogin.click();

				Thread.sleep(5000);

				WebElement buttonLoginPopup = driver.findElement(By.id("btn-login"));
				buttonLoginPopup.click();

				System.out.println(driver.getTitle());
				System.out.println("Teste concluído com sucesso.");
			}
        } catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
            driver.quit();
        }
    }
}
