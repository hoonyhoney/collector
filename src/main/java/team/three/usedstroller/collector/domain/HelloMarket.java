package team.three.usedstroller.collector.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HelloMarket {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String title;
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	private String link;
	private String price;
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	private String imgSrc;

	@Builder
	public HelloMarket(String title, String link, String price, String imgSrc) {
		this.title = title;
		this.link = link;
		this.price = price;
		this.imgSrc = imgSrc;
	}
}
