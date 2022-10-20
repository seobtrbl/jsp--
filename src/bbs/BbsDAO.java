package bbs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class BbsDAO {
	// 데이터베이스 접근 객체

	private Connection conn;
	private ResultSet rs;
	// preparedstatement 객체 위에서 빼주는 이유?
	// 여러개의 함수를 쓰다보니 DB에서 마찰이 일어나지 않도록 내부에서 써줌

	public BbsDAO() {
		try {
			String dbURL = "jdbc:mysql://localhost:3306/BBS?serverTimezone=UTC";
			String dbID = "root";
			String dbPW = "root";
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(dbURL, dbID, dbPW);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getDate() { // 현재의 시간을 가져오는 함수
		String SQL = "SELECT NOW()";

		try {
			PreparedStatement pstmt = conn.prepareStatement(SQL);
			rs = pstmt.executeQuery();
			if (rs.next()) { // 결과가 있는 경우?
				return rs.getString(1); // 현재 날짜 반환
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return ""; // 문자열 반환하면서 데이터베이스 오류 안내
	}

	public int getNext() {
		String SQL = "SELECT bbsID FROM BBS ORDER BY bbsID DESC";

		try {
			PreparedStatement pstmt = conn.prepareStatement(SQL);
			rs = pstmt.executeQuery();
			if (rs.next()) { // 결과가 있는 경우?
				return rs.getInt(1) + 1; // 게시글의 번호에 1을 더하면 다음 게시글의 번호
			}
			return 1; // 첫번째 게시글인 경우
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1; // 데이터베이스 오류
	}

	public int write(String bbsTitle, String userID, String bbsContent) {
		String SQL = "INSERT INTO bbs(bbsID, bbsTitle, userID, bbsDate, bbscontent,bbsAvailable)VALUES(?,?,?,?,?,?)";

		try {
			PreparedStatement pstmt = conn.prepareStatement(SQL);
			pstmt.setInt(1, getNext());
			pstmt.setString(2, bbsTitle);
			pstmt.setString(3, userID);
			pstmt.setString(4, getDate());
			pstmt.setString(5, bbsContent);
			pstmt.setInt(6, 1); // available 글이 삭제 되는거니까 1
			// insert 타입은 rs=pstmt.executeQuery(); 하지 않음.
			// 수행결과로 resultset 객체의 값을 반환함
			// select 구문 사용시!
			return pstmt.executeUpdate();
			// 수행결과로 Int타입의 값을 반환한다.
			// select 구문을 제외한 다른 구문을 수행할 때 사용되는 함수이다.
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1; // 데이터베이스 오류
	}

	public ArrayList<Bbs> getList(int pageNumber) {
		String SQL = "SELECT * FROM BBS WHERE bbsID < ? and bbsAvailable = 1 order by bbsID DESC LIMIT 10";
		ArrayList<Bbs> list = new ArrayList<Bbs>();

		try {
			PreparedStatement pstmt = conn.prepareStatement(SQL);
			pstmt.setInt(1, getNext() - (pageNumber - 1) * 10);
			rs = pstmt.executeQuery();
			while (rs.next()) { // 결과가 나올 때 마다!
				Bbs bbs = new Bbs();
				bbs.setBbsID(rs.getInt(1));
				bbs.setBbsTitle(rs.getString(2));
				bbs.setUserID(rs.getString(3));
				bbs.setBbsDate(rs.getString(4));
				bbs.setBbsContent(rs.getString(5));
				bbs.setBbsAvailable(rs.getInt(6));
				list.add(bbs);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public boolean nextPage(int pageNumber) {
		// 만약에 페이지가 10단위로 끊긴다면 (페이징)
		// 다음페이지는 없어야함. 10단위로 끊길 때 다음페이지가 없다는거 알려주기 위해서
		String SQL = "SELECT * FROM BBS WHERE bbsID < ? and bbsAvailable = 1 order by bbsID DESC LIMIT 10";

		try {
			PreparedStatement pstmt = conn.prepareStatement(SQL);
			pstmt.setInt(1, getNext() - (pageNumber - 1) * 10);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				return true;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public Bbs getBbs (int bbsID) { //게시판 글을 불러오는 함수, 상세내용
		String SQL = "SELECT * FROM BBS WHERE bbsID = ?";
		//bbsID에 따른 값에 숫자에 해당하는 게시글을 가져온다

		try {
			PreparedStatement pstmt = conn.prepareStatement(SQL);
			pstmt.setInt(1, bbsID);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				Bbs bbs = new Bbs();
				bbs.setBbsID(rs.getInt(1));
				bbs.setBbsTitle(rs.getString(2));
				bbs.setUserID(rs.getString(3));
				bbs.setBbsDate(rs.getString(4));
				bbs.setBbsContent(rs.getString(5));
				bbs.setBbsAvailable(rs.getInt(6));
				return bbs;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
