package com.buka.util;

import com.buka.vo.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * @author lhb
 * @version 1.0
 * @description: JWt工具类
 * @date 2025/2/16 下午3:05
 */
@Slf4j
public class JWTUtil {


    /**
     * token 过期时间，正常是7天，方便测试我们改为70
     */
    private static final long EXPIRE = 1000 * 60 * 60 * 24 * 7 * 10;

    /**
     * 加密的秘钥
     */
    private static final String SECRET = "buka2025";

    /**
     * 令牌前缀
     */
    private static final String TOKEN_PREFIX = "buka";

    /**
     * subject
     */
    private static final String SUBJECT = "buka";

    /**
    * @Author: lhb
    * @Description: 根据用户信息，生成令牌
    * @DateTime: 下午3:07 2025/2/16
    * @Params: [loginUser]
    * @Return java.lang.String
    */
    public static String geneJsonWebToken(LoginUser loginUser) {

        if (loginUser == null) {
            throw new NullPointerException("loginUser对象为空");
        }

        String token = Jwts.builder().setSubject(SUBJECT)
                //payload
                .claim("head_img", loginUser.getHeadImg())
                .claim("id", loginUser.getId())
                .claim("name", loginUser.getName())
                .claim("mail", loginUser.getMail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE))
                .signWith(SignatureAlgorithm.HS256, SECRET).compact();

        token = TOKEN_PREFIX + token;
        return token;
    }



    /**
    * @Author: lhb
    * @Description: JET检验
    * @DateTime: 下午3:06 2025/2/16
    * @Params: [token]
    * @Return Claims
    */
    public static Claims checkJWT(String token) {

        try {

            final Claims claims = Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token.replace(TOKEN_PREFIX, "")).getBody();

            return claims;

        } catch (Exception e) {
            log.info("jwt token解密失败");
            return null;
        }
    }
}
