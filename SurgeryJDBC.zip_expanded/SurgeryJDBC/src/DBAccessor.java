import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;
import java.io.InputStream;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DBAccessor {
	private String dbname;
	private String host;
	private String port;
	private String user;
	private String passwd;
	private String schema;
	Connection conn = null;

	// TODO
	/**
	 * Initializes the class loading the database properties file and assigns
	 * values to the instance variables.
	 * 
	 * @throws RuntimeException
	 *             Properties file could not be found.
	 */
	public void init() {
		Properties prop = new Properties();
		InputStream propStream = this.getClass().getClassLoader().getResourceAsStream("db.properties");

		try {
			prop.load(propStream);
			this.host = prop.getProperty("host");
			this.port = prop.getProperty("port");
			this.dbname = prop.getProperty("dbname");
			this.schema = prop.getProperty("schema");
			this.passwd=prop.getProperty("passwd");
			this.user=prop.getProperty("user");
		} catch (IOException e) {
			String message = "ERROR: db.properties file could not be found";
			System.err.println(message);
			throw new RuntimeException(message, e);
		}
	}

	
	// TODO
	/**
	 * Obtains a {@link Connection} to the database, based on the values of the
	 * <code>db.properties</code> file.
	 * 
	 * @return DB connection or null if a problem occurred when trying to
	 *         connect.
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public Connection getConnection() throws SQLException, ClassNotFoundException {


			// Loads the driver
			Class.forName("org.postgresql.Driver");
			String url="jdbc:postgresql://"+host+":"+port+"/"+dbname;
			
			conn=DriverManager.getConnection(url,user,passwd);
		
		conn.setAutoCommit(false);
		return conn;
	}

	// TODO
	public void altaVisita() throws SQLException, IOException, ParseException {
		
		try {
			BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
			Statement st=conn.createStatement();
			
			//PETICION DATOS PACIENTE
			System.out.println("Introduce el ID del paciente: ");
			int pat_number=Integer.parseInt(br.readLine());
			mostraPacient(pat_number);
			
			//PETICION DATOS DOCTOR
			System.out.println("Introduce el ID del doctor: ");
			int doc_number=Integer.parseInt(br.readLine());
			mostraDoctor(doc_number);
			
			//INTRODUCCION DE FECHA DE VISITA
			SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd");
			System.out.println("Intoduce la fecha de la visita: ");
			Date fecha=df.parse(br.readLine());
			
			//INSERCCION DE DATOS 
			st.executeUpdate("INSERT INTO visit VALUES("+doc_number+","+pat_number+",'"+fecha+"')");
			st.close();
			
			//INSERCCION CONFIRMADA!
			conn.commit();
			System.out.println("Insercción realizada correctamente.");

		} catch (SQLException e) {
			
			e.printStackTrace();
		}

	}

	// TODO
	public void modificaAdrecaPacient() throws SQLException, IOException {

		BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
		ResultSet rs=null;
		Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
		
		//INTRODUCIOMOS EL ID DEL PACIENTE
		System.out.println("Introduce el ID del paciente");
		int pat_number=Integer.parseInt(br.readLine());
		mostraPacient(pat_number);
		
		//REALIZAMOS CONSULTA
		rs=st.executeQuery("SELECT * FROM patient WHERE pat_number= "+pat_number);
		
		//PREGUNTAMOS SI SE QUIERE MODIFICAR LA DIRECCION
		System.out.print("Quieres cambiar su dirección?(si/no):");
		String resp=br.readLine();
		
		//EN CASO AFIRMATIVO CAMBIAMOS LA DIRECCION Y CIUDAD
		if(resp.equalsIgnoreCase("si")){
			rs.next();
			System.out.println("Introduce la nueva dirección:");
			rs.updateString("address", br.readLine());			
			System.out.println("Introduce la nueva ciudad del paciente: ");
			rs.updateString("city", br.readLine());
			rs.updateRow();
		}
		rs.close();
		st.close();
		
		//MOSTRAMOS AL PACIENTE CON LA DIRECCION MODIFICADA
		mostraPacient(pat_number);
	}

	//MOSTRAMOS LOS DOCTORES SEGUN SU ESPECIALIDAD
	public void mostraDoctorsPerEspecialitat() throws SQLException, IOException {

		mostraEspecialitats();
		
		BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Introduce el ID de la especialidad:");
		int id=Integer.parseInt(br.readLine());
		Statement st=conn.createStatement();
		ResultSet rs=st.executeQuery("SELECT doc_number, d.name, address,city,phone FROM doctor d, speciality s WHERE d.speciality=s.id AND s.id="+id);
		
		System.out.println("################################");
		System.out.println("####        DOCTORES         ###");
		System.out.println("################################\n");
		
		while(rs.next()){
			System.out.println("\tIdentificador: "+rs.getString("doc_number")+"\n\tNombre: "+rs.getString("name")+"\n\tDireccion: "+rs.getString("address")+"\n\tCiudad: "+rs.getString("city")+"\n\tTeléfono: "+rs.getString("phone"));
			System.out.println("\t----------");
		}
		rs.close();
		st.close();
	}

	//MOSTRAMOS LAS ESPECIALIDADES
	public void mostraEspecialitats() throws SQLException {

		ResultSet rs;
		Statement st=conn.createStatement();
		
		rs=st.executeQuery("SELECT id, name FROM speciality");
		
		while(rs.next()){
			System.out.println("\tID: "+rs.getString("id")+"\n"+"\tNombre: "+rs.getString("name"));
			System.out.println("\t----------");
		}
		rs.close();
		st.close();
	}

	//MOSTRAMOS LOS PACIENTES SEGUN SU NUMERO DE IDENTIFICACION
	public void mostraPacient(int pat_number) throws SQLException {

		
		Statement st=conn.createStatement();
		ResultSet rs=st.executeQuery("SELECT * FROM patient WHERE pat_number="+pat_number);
		
		while(rs.next()){
			
			System.out.println("\tNumero: "+rs.getString("pat_number")+"\n\tNombre: "+rs.getString("name")+"\n\tDirección: "+rs.getString("address")+"\n\tCiudad: "+rs.getString("city")+"\n\tDNI: "+rs.getString("dni"));
			System.out.println("\t----------");
		}
		st.close();
		rs.close();
	}
	
	//MOSTRAMOS DOCTOR SEGUN SU NUMERO DE DOCTOR
	public void mostraDoctor(int doc_number) throws SQLException {

		Statement st=conn.createStatement();
		ResultSet rs=st.executeQuery("SELECT d.name, s.name, d.salary FROM doctor d, speciality s WHERE d.speciality=s.id AND doc_number="+doc_number);
		
		while(rs.next()){
			
			System.out.println("\tNombre: "+rs.getString(1)+"\n\tEspeciaidad: "+rs.getString(2)+"\n\tSalario: "+rs.getString(3));
			System.out.println("\t----------");
		}
		st.close();
		rs.close();
	}

	//MODIFICA SALARIO DE DOCTOR
	public void modificaSalariDoctor() throws SQLException, NumberFormatException, IOException {

		BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
		ResultSet rs=null;
		Statement st=conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);
		//PEDIMOS EL IDENTIFICADOR DEL DOCTOR
		System.out.println("Introduce el ID del doctor: ");
		int doc_number=Integer.parseInt(br.readLine());
		
		rs=st.executeQuery("SELECT * FROM doctor WHERE doc_number="+doc_number);
		
		//MOSTRAMOS LOS DATOS DEL DOCTOR INDICADO
		mostraDoctor(doc_number);
		
		System.out.println("¿Quieres cambiar el salario del doctor?(si/no): ");
		String resp=br.readLine();
		
		//SI LA RESPUESTA ES POSITIVA...
		if(resp.equalsIgnoreCase("si")){
			
			rs.next();
			System.out.println("Introduce el nuevo salario: ");
			rs.updateFloat("salary", Float.parseFloat(br.readLine()));
			rs.updateRow();
		}
		rs.close();
		st.close();
		
		//MOSTRAMOS EL DOCTOR CON EL CAMBIO DE SALARIO
		mostraDoctor(doc_number);
	}

	public void sortir() throws SQLException {
		System.out.println("ADÉU!");
		conn.close();
		System.exit(0);
	}

	// TODO
	public void carregaVisites() throws SQLException, NumberFormatException, IOException, ParseException {

		// TODO
		// mitjançant Prepared Statement
		// per a cada línia del fitxer visites.csv
		// realitzar la inserció corresponent
		
		String sql="INSERT INTO visit (doc_number,pat_number,visit_date,price) VALUES(?,?,?,?)";
		PreparedStatement pst=conn.prepareStatement(sql);
		
		BufferedReader br=new BufferedReader(new FileReader("visites.csv"));
		
		String linea=br.readLine();
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd");
		
		while(linea!=null){
			
			StringTokenizer token=new StringTokenizer(linea , ",");
			
			while(token.hasMoreTokens()){
				pst.setInt(1, Integer.parseInt(token.nextToken()));
				pst.setInt(2, Integer.parseInt(token.nextToken()));
				pst.setDate(3, java.sql.Date.valueOf(token.nextToken()));
				pst.setFloat(4, Float.parseFloat(token.nextToken()));
			}
			
			linea=br.readLine();
			pst.executeUpdate();		
		}
		pst.close();
	}
}
