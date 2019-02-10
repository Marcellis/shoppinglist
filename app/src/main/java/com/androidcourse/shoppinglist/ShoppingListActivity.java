package com.androidcourse.shoppinglist;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShoppingListActivity extends AppCompatActivity implements ShoppingListAdapter.ItemClickListener {

	public final static int TASK_INSERT_PRODUCT = 0;
	public final static int TASK_DELETE_PRODUCT = 1;
	public final static int TASK_GET_ALL_PRODUCTS = 2;
	public final static int TASK_DELETE_ALL_PRODUCTS = 3;

	private RecyclerView rvShoppingList;
	private EditText etInput;
	private ShoppingListAdapter shoppingListAdapter;
	private List<Product> shoppingList = new ArrayList<>();
	private ProductRoomDatabase db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shopping_list);
		etInput = findViewById(R.id.et_input);
		db = ProductRoomDatabase.getDatabase(this);

		initToolbar();
		initRecyclerView();
		initFloatingActionButton();
	}

	/**
	 * Setup the Add Button.
	 */
	private void initFloatingActionButton() {
		FloatingActionButton fab = findViewById(R.id.fab_add);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String input = etInput.getText().toString();
				Product product = new Product(input);
				new ProductAsyncTask(TASK_INSERT_PRODUCT).execute(product);
			}
		});
	}

	/**
	 * Add the toolbar to the activity and set the title.
	 */
	private void initToolbar() {
		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setTitle("Shopping List");
		setSupportActionBar(toolbar);
	}

	/**
	 * Initialize the shopping list recycler view with the ShoppingListAdapter and fill the
	 * list with the products stored in the ProductsRoomDatabase.
	 */
	private void initRecyclerView() {
		shoppingListAdapter = new ShoppingListAdapter(shoppingList, this);
		rvShoppingList = findViewById(R.id.rv_shopping_list);
		rvShoppingList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
		rvShoppingList.setAdapter(shoppingListAdapter);
		rvShoppingList.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
		new ProductAsyncTask(TASK_GET_ALL_PRODUCTS).execute();
	}

	/**
	 * Notify the adapter that the data set has changed.
	 */
	private void updateUI() {
		shoppingListAdapter.notifyDataSetChanged();
	}

	/**
	 * Remove a product from the database when an item has been long clicked.
	 * @param product Product to be removed.
	 */
	@Override
	public void onItemLongClick(Product product) {
		new ProductAsyncTask(TASK_DELETE_PRODUCT).execute(product);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_shopping_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Checking the item id of our menu item.
		if (item.getItemId() == R.id.action_delete_item) {
			// Deleting all items and notifying our list adapter of the changes.
			new ProductAsyncTask(TASK_DELETE_ALL_PRODUCTS).execute();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Class which handles the ProductRoomDatabase transactions asynchronously.
	 */
	public class ProductAsyncTask extends AsyncTask<Product, Void, List<Product>> {

		private int taskCode;

		public ProductAsyncTask(int taskCode) {
			this.taskCode = taskCode;
		}

		@Override
		protected List doInBackground(Product... products) {
			switch (taskCode) {
				case TASK_INSERT_PRODUCT:
					db.productDao().insert(products[0]);
					break;

				case TASK_DELETE_PRODUCT:
					db.productDao().delete(products[0]);
					break;

				case TASK_DELETE_ALL_PRODUCTS:
					db.productDao().delete(shoppingList);
					break;
			}

			//To return a new list with the updated data, we get all the data from the database again.
			return db.productDao().getAllProducts();
		}

		@Override
		protected void onPostExecute(List<Product> list) {
			super.onPostExecute(list);
			shoppingList.clear();
			shoppingList.addAll(list);
			updateUI();
		}
	}

}
