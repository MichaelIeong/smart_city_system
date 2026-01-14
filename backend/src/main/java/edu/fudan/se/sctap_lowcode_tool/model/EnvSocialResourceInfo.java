package edu.fudan.se.sctap_lowcode_tool.model;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "social_resources")
@Data
@EqualsAndHashCode(callSuper = true)
public class EnvSocialResourceInfo extends BaseResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}