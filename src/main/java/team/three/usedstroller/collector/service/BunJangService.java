package team.three.usedstroller.collector.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import team.three.usedstroller.collector.domain.Product;
import team.three.usedstroller.collector.domain.SourceType;
import team.three.usedstroller.collector.repository.ProductRepository;
import team.three.usedstroller.collector.service.dto.BunjangApiResponse;
import team.three.usedstroller.collector.service.dto.BunjangItem;
import team.three.usedstroller.collector.util.ApiService;

@Slf4j
@Service
public class BunJangService extends CommonService {

	private final ApiService apiService;

	public BunJangService(ProductRepository productRepository, ApiService apiService) {
		super(productRepository);
		this.apiService = apiService;
	}

	ParameterizedTypeReference<BunjangApiResponse> typeReference = new ParameterizedTypeReference<>() {};

	public void start() {
		StopWatch stopWatch = new StopWatch();
		collecting()
				.doOnSubscribe(subscription -> stopWatch.start())
				.doOnSuccess(count -> {
					stopWatch.stop();
					log.info("번개장터 완료: {}건, 수집 시간: {}s", count, stopWatch.getTotalTimeSeconds());
				})
				.publishOn(Schedulers.boundedElastic())
				.doFinally(f -> super.deleteOldData(SourceType.BUNJANG))
				.subscribe();
	}

	/**
	 * api url: https://api.bunjang.co.kr/api/1/find_v2.json?q=%EC%9C%A0%EB%AA%A8%EC%B0%A8&page=0&n=200
	 * page: 0 (페이지 번호)
	 * n: 200 (한번에 가져오는 상품 수)
	 */
	public Mono<Integer> collecting() {
		AtomicInteger updateCount = new AtomicInteger(0);

		return getTotalPage()
			.flatMap(totalCount -> {
				int totalPage = totalCount / 200;
				log.info("bunjang total page: {}", totalPage);

				return Flux.range(0, totalPage)
						.flatMap(page -> {
							String url = "https://api.bunjang.co.kr/api/1/find_v2.json?q=%EC%9C%A0%EB%AA%A8%EC%B0%A8&n=200&page=" + page;

							return apiService.apiCallGet(url, typeReference, MediaType.APPLICATION_JSON, true)
								.switchIfEmpty(Mono.defer(Mono::empty))
								.onErrorResume(e -> Mono.error(new RuntimeException("bunjang api connect error", e)))
								.publishOn(Schedulers.boundedElastic())
								.flatMap(res -> saveItemList(res.getList())
										.flatMap(count -> {
											log.info("bunjang page: [{}], saved item: [{}], total update: [{}]", page, count, updateCount.addAndGet(count));
											return Mono.just(count);
										}));
						})
						.reduce(Integer::sum);
			});
	}

	public Mono<Integer> saveItemList(List<BunjangItem> list) {
		return Flux.fromIterable(list)
				.flatMap(Product::createBunJang)
				.collectList()
				.flatMap(this::saveProducts);
	}

	private Mono<Integer> getTotalPage() {
		String url = "https://api.bunjang.co.kr/api/1/find_v2.json?q=%EC%9C%A0%EB%AA%A8%EC%B0%A8&page=0&n=0";
		return apiService.apiCallGet(url, typeReference, MediaType.APPLICATION_JSON, true)
				.switchIfEmpty(Mono.defer(Mono::empty))
				.flatMap(res -> Mono.just(res.getNumFound()))
				.onErrorResume(e -> Mono.error(new RuntimeException("bunjang totalCount api connect error", e)));
	}

}
