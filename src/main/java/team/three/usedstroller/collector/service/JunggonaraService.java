package team.three.usedstroller.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import team.three.usedstroller.collector.config.ChromiumDriver;
import team.three.usedstroller.collector.domain.Junggo;
import team.three.usedstroller.collector.repository.JunggonaraRepository;

import java.util.ArrayList;
import java.util.List;

import static io.netty.util.internal.StringUtil.EMPTY_STRING;

@Service
@Slf4j
@RequiredArgsConstructor
public class JunggonaraService {

	private final ChromiumDriver driver;
	private final JunggonaraRepository junggonaraRepository;

	public int collectingJunggonara(int startPage, int endPage) {
		int complete = 0;
//		int pageTotal = getTotalPageJungGo();
//		log.info("junggonara total page: {}", pageTotal);

		for (int i = startPage; i <= endPage; i++) {
			String url = "https://web.joongna.com/search/%EC%9C%A0%EB%AA%A8%EC%B0%A8?page=" + i;
			driver.open(url);
			driver.wait(1500);

			List<WebElement> list = driver.findAllByXpath("//*[@id='__next']/div/main/div[1]/div[2]/ul/li");
			if (ObjectUtils.isEmpty(list)) {
				return complete;
			}

			int size = saveItemList(list);
			complete += size;
			log.info("junggonara page: [{}], saved item: [{}]", i, complete);
		}

		return complete;
	}

	@Transactional
	public int saveItemList(List<WebElement> list) {
		List<Junggo> items = new ArrayList<>();
		String img;
		String price;
		String title;
		String link;
		String address;
		String uploadTime;

		for (WebElement element : list) {
			if (!ObjectUtils.isEmpty(element.getAttribute("class")) || ObjectUtils.isEmpty(element.getText())) {
				continue;
			}
			title = element.findElement(By.xpath(".//h2")).getText();
			link = element.findElement(By.xpath(".//a")).getAttribute("href");
			price = element.findElement(By.xpath(".//a/div[2]/div[1]")).getText();
			img = element.findElement(By.xpath(".//a/div[1]/img")).getAttribute("src");
			address = element.findElements(By.xpath(".//a/div[2]/div[2]/span[1]"))
					.stream()
					.findFirst()
					.map(WebElement::getText)
					.orElseGet(() -> EMPTY_STRING);
			uploadTime = element.findElements(By.xpath(".//a/div[2]/div[2]/span[3]"))
					.stream()
					.findFirst()
					.map(WebElement::getText)
					.orElseGet(() -> EMPTY_STRING);

			Junggo junggo = Junggo.builder()
					.title(title)
					.link(link)
					.price(price)
					.imgSrc(img)
					.address(address)
					.uploadTime(uploadTime)
					.build();
			items.add(junggo);
		}

		junggonaraRepository.saveAll(items);
		return items.size();
	}

	private int getTotalPageJungGo() {
		int qtyPerPage = 86;
		String url = "https://web.joongna.com/search/%EC%9C%A0%EB%AA%A8%EC%B0%A8?page=1";
		driver.open(url);
		WebElement content = driver.findOneByXpath("//*[@id=\"__next\"]");
		String totalQty = content.findElement(By.xpath("//*[@id=\"__next\"]/div/main/div[1]/div[2]/div[2]/div/div/div[1]")).getText();
		String intStr = totalQty.replaceAll("[^0-9]", "");
		int totalQtyInt = Integer.parseInt(intStr);
		return (int) Math.ceil((double) totalQtyInt / qtyPerPage);
	}

}