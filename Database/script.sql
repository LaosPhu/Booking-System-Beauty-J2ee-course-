USE [master]
GO
/****** Object:  Database [BlissSpa]    Script Date: 5/21/2025 1:38:45 PM ******/
CREATE DATABASE [BlissSpa]
 CONTAINMENT = NONE
 ON  PRIMARY 
( NAME = N'BlissSpa', FILENAME = N'/var/opt/mssql/data/BlissSpa.mdf' , SIZE = 8192KB , MAXSIZE = UNLIMITED, FILEGROWTH = 65536KB )
 LOG ON 
( NAME = N'BlissSpa_log', FILENAME = N'/var/opt/mssql/data/BlissSpa_log.ldf' , SIZE = 8192KB , MAXSIZE = 2048GB , FILEGROWTH = 65536KB )
 WITH CATALOG_COLLATION = DATABASE_DEFAULT
GO
ALTER DATABASE [BlissSpa] SET COMPATIBILITY_LEVEL = 150
GO
IF (1 = FULLTEXTSERVICEPROPERTY('IsFullTextInstalled'))
begin
EXEC [BlissSpa].[dbo].[sp_fulltext_database] @action = 'enable'
end
GO
ALTER DATABASE [BlissSpa] SET ANSI_NULL_DEFAULT OFF 
GO
ALTER DATABASE [BlissSpa] SET ANSI_NULLS OFF 
GO
ALTER DATABASE [BlissSpa] SET ANSI_PADDING OFF 
GO
ALTER DATABASE [BlissSpa] SET ANSI_WARNINGS OFF 
GO
ALTER DATABASE [BlissSpa] SET ARITHABORT OFF 
GO
ALTER DATABASE [BlissSpa] SET AUTO_CLOSE OFF 
GO
ALTER DATABASE [BlissSpa] SET AUTO_SHRINK OFF 
GO
ALTER DATABASE [BlissSpa] SET AUTO_UPDATE_STATISTICS ON 
GO
ALTER DATABASE [BlissSpa] SET CURSOR_CLOSE_ON_COMMIT OFF 
GO
ALTER DATABASE [BlissSpa] SET CURSOR_DEFAULT  GLOBAL 
GO
ALTER DATABASE [BlissSpa] SET CONCAT_NULL_YIELDS_NULL OFF 
GO
ALTER DATABASE [BlissSpa] SET NUMERIC_ROUNDABORT OFF 
GO
ALTER DATABASE [BlissSpa] SET QUOTED_IDENTIFIER OFF 
GO
ALTER DATABASE [BlissSpa] SET RECURSIVE_TRIGGERS OFF 
GO
ALTER DATABASE [BlissSpa] SET  DISABLE_BROKER 
GO
ALTER DATABASE [BlissSpa] SET AUTO_UPDATE_STATISTICS_ASYNC OFF 
GO
ALTER DATABASE [BlissSpa] SET DATE_CORRELATION_OPTIMIZATION OFF 
GO
ALTER DATABASE [BlissSpa] SET TRUSTWORTHY OFF 
GO
ALTER DATABASE [BlissSpa] SET ALLOW_SNAPSHOT_ISOLATION OFF 
GO
ALTER DATABASE [BlissSpa] SET PARAMETERIZATION SIMPLE 
GO
ALTER DATABASE [BlissSpa] SET READ_COMMITTED_SNAPSHOT OFF 
GO
ALTER DATABASE [BlissSpa] SET HONOR_BROKER_PRIORITY OFF 
GO
ALTER DATABASE [BlissSpa] SET RECOVERY FULL 
GO
ALTER DATABASE [BlissSpa] SET  MULTI_USER 
GO
ALTER DATABASE [BlissSpa] SET PAGE_VERIFY CHECKSUM  
GO
ALTER DATABASE [BlissSpa] SET DB_CHAINING OFF 
GO
ALTER DATABASE [BlissSpa] SET FILESTREAM( NON_TRANSACTED_ACCESS = OFF ) 
GO
ALTER DATABASE [BlissSpa] SET TARGET_RECOVERY_TIME = 60 SECONDS 
GO
ALTER DATABASE [BlissSpa] SET DELAYED_DURABILITY = DISABLED 
GO
ALTER DATABASE [BlissSpa] SET ACCELERATED_DATABASE_RECOVERY = OFF  
GO
EXEC sys.sp_db_vardecimal_storage_format N'BlissSpa', N'ON'
GO
ALTER DATABASE [BlissSpa] SET QUERY_STORE = OFF
GO
USE [BlissSpa]
GO
/****** Object:  Table [dbo].[booking]    Script Date: 5/21/2025 1:38:45 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[booking](
	[booking_date] [date] NOT NULL,
	[total_price] [numeric](38, 2) NOT NULL,
	[appointment_date_time] [datetime2](6) NOT NULL,
	[booking_id] [bigint] IDENTITY(1,1) NOT NULL,
	[customer_id] [bigint] NOT NULL,
	[booking_status] [varchar](255) NULL,
	[payment_method] [varchar](255) NOT NULL,
	[message] [varchar](255) NULL,
PRIMARY KEY CLUSTERED 
(
	[booking_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[booking_details]    Script Date: 5/21/2025 1:38:45 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[booking_details](
	[booking_details_id] [bigint] IDENTITY(1,1) NOT NULL,
	[booking_id] [bigint] NOT NULL,
	[service_id] [bigint] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[booking_details_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[payment]    Script Date: 5/21/2025 1:38:45 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[payment](
	[booking_id] [bigint] NOT NULL,
	[payment_date] [datetime2](6) NOT NULL,
	[payment_id] [bigint] IDENTITY(1,1) NOT NULL,
	[payment_status] [varchar](255) NULL,
	[transaction_id] [varchar](255) NULL,
	[amount] [numeric](38, 2) NULL,
	[payment_method] [varchar](255) NULL,
	[response_code] [varchar](255) NULL,
PRIMARY KEY CLUSTERED 
(
	[payment_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[service]    Script Date: 5/21/2025 1:38:45 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[service](
	[duration] [int] NULL,
	[price] [numeric](38, 2) NOT NULL,
	[service_id] [bigint] IDENTITY(1,1) NOT NULL,
	[category] [varchar](255) NULL,
	[description] [text] NULL,
	[imageurl] [varchar](255) NULL,
	[name] [varchar](255) NOT NULL,
	[status] [bit] NULL,
PRIMARY KEY CLUSTERED 
(
	[service_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UKadgojnrwwx9c3y3qa2q08uuqp] UNIQUE NONCLUSTERED 
(
	[name] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[user]    Script Date: 5/21/2025 1:38:45 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[user](
	[registration_date] [datetime2](6) NULL,
	[user_id] [bigint] IDENTITY(1,1) NOT NULL,
	[address] [varchar](255) NULL,
	[email] [varchar](255) NOT NULL,
	[first_name] [varchar](255) NOT NULL,
	[last_name] [varchar](255) NOT NULL,
	[notes] [text] NULL,
	[password] [varchar](255) NULL,
	[phone_number] [varchar](255) NULL,
	[role] [varchar](255) NOT NULL,
	[username] [varchar](255) NULL,
	[status] [bit] NULL,
	[auth_provider] [varchar](255) NULL,
	[provider_id] [varchar](255) NULL,
PRIMARY KEY CLUSTERED 
(
	[user_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UKhl4ga9r00rh51mdaf20hmnslt] UNIQUE NONCLUSTERED 
(
	[email] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
ALTER TABLE [dbo].[booking]  WITH CHECK ADD  CONSTRAINT [FKonc9rmatlpm8mma9qhhexmbo7] FOREIGN KEY([customer_id])
REFERENCES [dbo].[user] ([user_id])
GO
ALTER TABLE [dbo].[booking] CHECK CONSTRAINT [FKonc9rmatlpm8mma9qhhexmbo7]
GO
ALTER TABLE [dbo].[booking_details]  WITH CHECK ADD  CONSTRAINT [FK2qbovjg2h0kmpct08w8hrv6vj] FOREIGN KEY([service_id])
REFERENCES [dbo].[service] ([service_id])
GO
ALTER TABLE [dbo].[booking_details] CHECK CONSTRAINT [FK2qbovjg2h0kmpct08w8hrv6vj]
GO
ALTER TABLE [dbo].[booking_details]  WITH CHECK ADD  CONSTRAINT [FKdi4hhcv3pwr6b14qfhf9gahex] FOREIGN KEY([booking_id])
REFERENCES [dbo].[booking] ([booking_id])
GO
ALTER TABLE [dbo].[booking_details] CHECK CONSTRAINT [FKdi4hhcv3pwr6b14qfhf9gahex]
GO
ALTER TABLE [dbo].[payment]  WITH CHECK ADD  CONSTRAINT [FKqewrl4xrv9eiad6eab3aoja65] FOREIGN KEY([booking_id])
REFERENCES [dbo].[booking] ([booking_id])
GO
ALTER TABLE [dbo].[payment] CHECK CONSTRAINT [FKqewrl4xrv9eiad6eab3aoja65]
GO
ALTER TABLE [dbo].[user]  WITH CHECK ADD CHECK  (([auth_provider]='GOOGLE' OR [auth_provider]='LOCAL'))
GO
USE [master]
GO
ALTER DATABASE [BlissSpa] SET  READ_WRITE 
GO
