package team.three.usedstroller.collector.domain;

import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "naver_shopping")
public class NaverShopping extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 1000)
	private String title;

	private String price;

	@Lob
	@Type(type = "org.hibernate.type.TextType")
	private String link;

	@Lob
	@Type(type = "org.hibernate.type.TextType")
	private String imgSrc;

	@Builder
	public NaverShopping(String title, String link, String price, String imgSrc) {
		this.title = title;
		this.link = link;
		this.price = price;
		this.imgSrc = imgSrc;
	}
}
