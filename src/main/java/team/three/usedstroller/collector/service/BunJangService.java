package team.three.usedstroller.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import team.three.usedstroller.collector.config.ChromiumDriver;
import team.three.usedstroller.collector.domain.BunJang;
import team.three.usedstroller.collector.repository.BunJangRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BunJangService {

	private final ChromiumDriver driver;
	private final BunJangRepository bunJangRepository;

	@Transactional
	public int collectingBunJang() {
		int complete = 0;
		int pageTotal = getTotalPageBunJang();
		log.info("bunjang total page: {}", pageTotal);

		for (int i = 1; i < pageTotal; i++) {
			String url = "https://m.bunjang.co.kr/search/products?order=score&page=" + i + "&q=%EC%9C%A0%EB%AA%A8%EC%B0%A8";
			driver.open(url);
			driver.wait(1);

			WebElement content = driver.getSelector("#root");
			List<WebElement> list = content.findElements(By.xpath("div/div/div[4]/div/div[4]/div/div"));
			if (ObjectUtils.isEmpty(list)) {
				return complete;
			}

			int size = saveItemList(list);
			complete += size;
			log.info("saved item: {}", complete);
		}

		return complete;
	}

	private int saveItemList(List<WebElement> list) {
		List<BunJang> items = new ArrayList<>();
		String img;
		String price;
		String title;
		String link;
		String address;
		String uploadTime;

		for (WebElement element : list) {
			title = element.findElement(By.xpath("a/div[2]/div[1]")).getText();
			link = element.findElement(By.xpath("a")).getAttribute("href");
			price = element.findElement(By.xpath("a/div[2]/div[2]/div[1]")).getText();
			img = element.findElement(By.xpath("a/div[1]/img")).getAttribute("src");
			address = element.findElement(By.xpath("a/div[3]")).getText();
			uploadTime = element.findElement(By.xpath("a/div[2]/div[2]/div[2]/span")).getText();

			BunJang bunJang = BunJang.builder()
					.title(title)
					.link(link)
					.price(price)
					.imgSrc(img)
					.address(address)
					.uploadTime(uploadTime)
					.build();
			items.add(bunJang);
		}

		bunJangRepository.saveAll(items);
		return items.size();
	}

	private int getTotalPageBunJang() {
		int qtyPerPage = 100;
		String url = "https://m.bunjang.co.kr/search/products?order=score&page=1&q=%EC%9C%A0%EB%AA%A8%EC%B0%A8";
		driver.open(url);
		WebElement content = driver.getXpath("//*[@id=\"root\"]");
		String totalQty = content.findElement(By.xpath("//*[@id=\"root\"]/div/div/div[4]/div/div[3]/div/div[1]/span[2]")).getText();
		String intStr = totalQty.replaceAll("[^0-9]", "");
		int totalQtyInt = Integer.parseInt(intStr);
		return (int) Math.ceil((double) totalQtyInt / qtyPerPage);
	}

}
