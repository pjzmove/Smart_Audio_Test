add_compile_definitions(WITH_POSIX)
add_compile_definitions(WITH_TCP)
add_compile_definitions(_GNU_SOURCE)

file(GLOB coap_src "libcoap/*.c")

list(REMOVE_ITEM coap_src "${PROJECT_SOURCE_DIR}/extlibs/libcoap/coap_io_lwip.c")

# Define this directory's flags:
#SET(COAP_C_FLAGS "-std=gnu99" CACHE STRING "Lib coap C FLAGS")
#SET(CMAKE_C_FLAGS "${COAP_C_FLAGS}")

add_library(coap STATIC ${coap_src} )

include_directories(connectivity_abstraction PUBLIC
  "${PROJECT_SOURCE_DIR}/include"
  "${PROJECT_SOURCE_DIR}/../../../../../iotivity/resource/c_common"
  "${PROJECT_SOURCE_DIR}/libcoap/include/coap"
  )
  
 add_library(mbedtls STATIC  
	"${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/debug.c"
	"${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/net_sockets.c"
	"${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/ssl_cache.c"
	"${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/ssl_ciphersuites.c"
	"${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/ssl_cli.c"
	"${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/ssl_cookie.c"
	"${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/ssl_srv.c"
	"${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/ssl_ticket.c"
	"${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/ssl_tls.c"
	)

 add_library(mbedcrypto STATIC
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/aes.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/aesni.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/arc4.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/asn1parse.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/asn1write.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/base64.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/bignum.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/blowfish.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/camellia.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/ccm.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/cipher.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/cipher_wrap.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/ctr_drbg.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/des.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/dhm.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/ecdh.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/ecdsa.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/ecjpake.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/ecp.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/ecp_curves.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/entropy.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/entropy_poll.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/error.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/gcm.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/havege.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/hmac_drbg.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/md.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/md2.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/md4.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/md5.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/md_wrap.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/memory_buffer_alloc.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/oid.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/padlock.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/pem.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/pk.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/pk_wrap.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/pkcs12.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/pkcs5.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/pkparse.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/pkwrite.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/platform.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/ripemd160.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/rsa.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/sha1.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/sha256.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/sha512.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/threading.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/timing.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/version.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/version_features.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/xtea.c"
    )

add_library(mbedx509 STATIC
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/certs.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/pkcs11.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/x509.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/x509_create.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/x509_crl.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/x509_crt.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/x509_csr.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/x509write_crt.c"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/library/x509write_csr.c"
    )

target_include_directories(mbedtls PUBLIC
		"${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/include"
		)
target_include_directories(mbedcrypto PUBLIC
		"${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/include"
		)
target_include_directories(mbedx509 PUBLIC
		"${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/include"
		)


	