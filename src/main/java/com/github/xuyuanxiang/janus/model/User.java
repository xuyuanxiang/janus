package com.github.xuyuanxiang.janus.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;

/**
 * 支付宝或微信用户（会员）信息。
 * <p>
 * 统一相同含义的字段名
 * 微信特有的字段以wechat开头
 * 支付宝独有的字段以alipay开头
 */
@NoArgsConstructor
@Data
public class User implements Serializable {

    @Builder
    public User(String id, Source source, String avatar, String province, String city, String nickName, Gender gender, String wechatUnionId, String wechatCountry, List<String> wechatPrivilege) {
        this.id = id;
        this.source = source;
        this.avatar = avatar;
        this.province = province;
        this.city = city;
        this.nickName = nickName;
        this.gender = gender.name();
        this.wechatUnionId = wechatUnionId;
        this.wechatCountry = wechatCountry;
        this.wechatPrivilege = wechatPrivilege;
    }

    public static User from(WechatGetUserResponse userResponse) {
        return User.builder()
            .id(userResponse.getOpenid())
            .source(Source.WECHAT)
            .avatar(userResponse.getHeadimgurl())
            .province(userResponse.getProvince())
            .city(userResponse.getCity())
            .nickName(userResponse.getNickname())
            .gender(Gender.from(userResponse.getSex()))
            .wechatCountry(userResponse.getCountry())
            .wechatPrivilege(userResponse.getPrivilege())
            .wechatUnionId(userResponse.getUnionid())
            .build();
    }

    public static User from(AlipayGetUserResponse.AlipayUserInfoShareResponse userResponse) {
        return User.builder()
            .id(userResponse.getUserId())
            .source(Source.ALIPAY)
            .avatar(userResponse.getAvatar())
            .province(userResponse.getProvince())
            .city(userResponse.getCity())
            .nickName(userResponse.getNickName())
            .gender(Gender.from(userResponse.getGender())).build();
    }

    /**
     * 支付宝userId或者微信openId
     */
    private String id;
    /**
     * 用户来源
     */
    private Source source;
    private String avatar;
    private String province;
    private String city;
    private String nickName;
    private String gender;
    /**
     * 微信特有字段
     */
    private String wechatUnionId;
    private String wechatCountry;
    private List<String> wechatPrivilege;

    public enum Source {
        /**
         * 支付宝用户
         */
        ALIPAY,
        /**
         * 微信用户
         */
        WECHAT;
    }

    public enum Gender {
        MALE,
        FEMALE,
        UNKNOWN;

        public static Gender from(String originValue) {
            if (StringUtils.equals("1", originValue) || StringUtils.equalsIgnoreCase("M", originValue)) {
                return MALE;
            } else if (StringUtils.equals("2", originValue) || StringUtils.equalsIgnoreCase("F", originValue)) {
                return FEMALE;
            } else {
                return UNKNOWN;
            }
        }
    }
}
