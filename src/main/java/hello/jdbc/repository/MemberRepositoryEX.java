package hello.jdbc.repository;

import hello.jdbc.domain.Member;

public interface MemberRepositoryEX {
 Member save(Member member);
 Member findById(String memberId);
 void update(String memberId, int money);
 void delete(String memberId);
}